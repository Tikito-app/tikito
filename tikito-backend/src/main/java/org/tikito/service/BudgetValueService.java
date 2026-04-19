package org.tikito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.DateRange;
import org.tikito.dto.money.MoneyTransactionGroupDto;
import org.tikito.entity.money.HistoricalBudgetValue;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.repository.HistoricalBudgetValueRepository;
import org.tikito.repository.MoneyTransactionRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@Slf4j
public class BudgetValueService {
    private final HistoricalBudgetValueRepository historicalBudgetValueRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;

    public BudgetValueService(final HistoricalBudgetValueRepository historicalBudgetValueRepository,
                              final MoneyTransactionRepository moneyTransactionRepository) {
        this.historicalBudgetValueRepository = historicalBudgetValueRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void generateValues(final long userId, final MoneyTransactionGroupDto group) {
        final LocalDate endDate = incrementByDateRange(group.getEndDate() == null ? LocalDate.now().plusYears(2) : group.getEndDate(), group.getDateRange(), 1);
        final Map<String, List<MoneyTransaction>> transactionsPerDateRange = new HashMap<>();
        final List<HistoricalBudgetValue> values = new ArrayList<>();

        moneyTransactionRepository
                .findByGroupId(group.getId())
                .forEach(transaction -> {
                    final String dateRangeString = getDateRangeString(LocalDate.ofInstant(transaction.getTimestamp(), ZoneId.systemDefault()), group.getDateRange());
                    transactionsPerDateRange.putIfAbsent(dateRangeString, new ArrayList<>());
                    transactionsPerDateRange.get(dateRangeString).add(transaction);
                });

        for (LocalDate currentDate = group.getStartDate();
             !currentDate.isAfter(endDate);
             currentDate = incrementByDateRange(currentDate, group.getDateRange(), 1)) {
            final String dateRangeString = getDateRangeString(currentDate, group.getDateRange());
            final HistoricalBudgetValue value = new HistoricalBudgetValue(userId, group, currentDate);

            if (transactionsPerDateRange.containsKey(dateRangeString)) {
                final List<MoneyTransaction> transactions = transactionsPerDateRange.get(dateRangeString);
                transactions.forEach(transaction -> apply(value, transaction));
            }

            values.add(value);
        }

        log.info("Persisting {} budget values", values.size());

        historicalBudgetValueRepository.deleteByUserIdAndGroupId(userId, group.getId());
        historicalBudgetValueRepository.saveAllAndFlush(values);
    }

    private void apply(final HistoricalBudgetValue value, final MoneyTransaction transaction) {
        value.setSpent(value.getSpent() + transaction.getAmount());
    }

    private LocalDate incrementByDateRange(final LocalDate date, final DateRange dateRange, final int amount) {
        return switch (dateRange) {
            case YEAR, ALL -> date.plusYears(amount).withDayOfYear(1);
            case MONTH -> date.plusMonths(amount).withDayOfMonth(1);
            case WEEK -> date.plusWeeks(amount).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case DAY -> date.plusDays(amount);
            case ONCE -> date.minusYears(9999); // once implies to deal with once, todo: check if this works
        };
    }

    private String getDateRangeString(final LocalDate date, final DateRange dateRange) {
        return switch (dateRange) {
            case YEAR -> Integer.toString(date.getYear());
            case MONTH -> date.getMonthValue() + "-" + date.getYear();
            case WEEK -> date.get(WeekFields.of(Locale.getDefault()).weekOfYear()) + "-" + date.getYear();
            case DAY -> date.getDayOfMonth() + "-" + date.getMonthValue() + "-" + date.getYear();
            case ALL, ONCE -> "";
        };
    }
}
