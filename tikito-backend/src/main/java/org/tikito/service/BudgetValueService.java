package org.tikito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.DateRange;
import org.tikito.entity.budget.Budget;
import org.tikito.entity.budget.HistoricalBudgetValue;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.repository.BudgetRepository;
import org.tikito.repository.HistoricalBudgetValueRepository;
import org.tikito.repository.MoneyTransactionRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BudgetValueService {
    private final HistoricalBudgetValueRepository historicalBudgetValueRepository;
    private final BudgetRepository budgetRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;

    public BudgetValueService(final HistoricalBudgetValueRepository historicalBudgetValueRepository,
                              final BudgetRepository budgetRepository,
                              final MoneyTransactionRepository moneyTransactionRepository) {
        this.historicalBudgetValueRepository = historicalBudgetValueRepository;
        this.budgetRepository = budgetRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    void generateValues(final long userId, final long budgetId) {
        final Budget budget = budgetRepository.findByUserIdAndId(userId, budgetId).orElseThrow();
        final LocalDate endDate = incrementByDateRange(budget.getEndDate() == null ? LocalDate.now() : budget.getEndDate(), budget.getDateRange(), 1);
        final Map<String, List<MoneyTransaction>> transactionsPerDateRange = new HashMap<>();
        final List<HistoricalBudgetValue> values = new ArrayList<>();

        moneyTransactionRepository
                .findByGroupIdIn(budget.getGroups().stream().map(MoneyTransactionGroup::getId).collect(Collectors.toSet()))
                .forEach(transaction -> {
                    final String dateRangeString = getDateRangeString(LocalDate.ofInstant(transaction.getTimestamp(), ZoneId.systemDefault()), budget.getDateRange());
                    transactionsPerDateRange.putIfAbsent(dateRangeString, new ArrayList<>());
                    transactionsPerDateRange.get(dateRangeString).add(transaction);
                });

        for (LocalDate currentDate = budget.getStartDate();
            !currentDate.isAfter(endDate);
            currentDate = incrementByDateRange(currentDate, budget.getDateRange(), 1)) {
            final String dateRangeString = getDateRangeString(currentDate, budget.getDateRange());
            final HistoricalBudgetValue value = new HistoricalBudgetValue(userId, budget.getId(), currentDate);

            if(transactionsPerDateRange.containsKey(dateRangeString)) {
                final List<MoneyTransaction> transactions = transactionsPerDateRange.get(dateRangeString);
                transactions.forEach(transaction -> apply(value, transaction));
            }

            values.add(value);
        }

        log.info("Persisting {} budget values", values.size());

        historicalBudgetValueRepository.deleteByUserIdAndBudgetId(userId, budgetId);
        historicalBudgetValueRepository.saveAllAndFlush(values);
    }

    private void apply(final HistoricalBudgetValue value, final MoneyTransaction transaction) {
        value.setSpent(value.getSpent() + transaction.getAmount());
    }

    private LocalDate incrementByDateRange(final LocalDate date, final DateRange dateRange, final int amount) {
        return switch (dateRange) {
            case YEAR, ALL -> date.plusYears(amount);
            case MONTH -> date.plusMonths(amount);
            case WEEK -> date.plusWeeks(amount);
            case DAY -> date.plusDays(amount);
        };
    }

    private String getDateRangeString(final LocalDate date, final DateRange dateRange) {
        return switch (dateRange) {
            case YEAR -> Integer.toString(date.getYear());
            case MONTH -> date.getMonthValue() + "-" + date.getYear();
            case WEEK -> date.get(WeekFields.of(Locale.getDefault()).weekOfYear()) + "-" + date.getYear();
            case DAY -> date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear();
            case ALL -> "";
        };
    }
}
