package org.tikito.service.money;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.AccountDto;
import org.tikito.dto.money.AggregatedHistoricalMoneyHoldingValueDto;
import org.tikito.dto.money.HistoricalMoneyHoldingValueDto;
import org.tikito.entity.Job;
import org.tikito.entity.money.AggregatedHistoricalMoneyHoldingValue;
import org.tikito.entity.money.HistoricalMoneyHoldingValue;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.repository.AccountRepository;
import org.tikito.repository.AggregatedHistoricalMoneyHoldingValueRepository;
import org.tikito.repository.HistoricalMoneyHoldingValueRepository;
import org.tikito.repository.MoneyTransactionRepository;
import org.tikito.service.CacheService;
import org.tikito.service.job.JobProcessor;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static org.tikito.service.job.JobType.RECALCULATE_AGGREGATED_HISTORICAL_MONEY_VALUES;
import static org.tikito.service.job.JobType.RECALCULATE_HISTORICAL_MONEY_VALUES;

@Slf4j
@Service
public class MoneyHoldingService implements JobProcessor {
    private final HistoricalMoneyHoldingValueRepository historicalMoneyHoldingValueRepository;
    private final AccountRepository accountRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final CacheService cacheService;
    private final AggregatedHistoricalMoneyHoldingValueRepository aggregatedHistoricalMoneyHoldingValueRepository;

    public MoneyHoldingService(final HistoricalMoneyHoldingValueRepository historicalMoneyHoldingValueRepository,
                               final AccountRepository accountRepository,
                               final MoneyTransactionRepository moneyTransactionRepository,
                               final CacheService cacheService,
                               final AggregatedHistoricalMoneyHoldingValueRepository aggregatedHistoricalMoneyHoldingValueRepository) {
        this.historicalMoneyHoldingValueRepository = historicalMoneyHoldingValueRepository;
        this.accountRepository = accountRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.cacheService = cacheService;
        this.aggregatedHistoricalMoneyHoldingValueRepository = aggregatedHistoricalMoneyHoldingValueRepository;
    }

    public List<AggregatedHistoricalMoneyHoldingValueDto> getAggregatedHoldingValues(final long userId) {
        return aggregatedHistoricalMoneyHoldingValueRepository
                .findAllByUserId(userId)
                .stream()
                .map(AggregatedHistoricalMoneyHoldingValue::toDto)
                .sorted(Comparator.comparing(AggregatedHistoricalMoneyHoldingValueDto::getDate))
                .toList();
    }


    @Transactional(propagation = Propagation.MANDATORY)
    public void recalculateHistoricalHoldingValues(final long userId, final long accountId) {
        final AccountDto account = accountRepository.findById(accountId).orElseThrow().toDto();
        final Map<LocalDate, List<MoneyTransaction>> transactionsPerTimestamp = getTransactionsPerTimestamp(accountId);
        if (transactionsPerTimestamp.isEmpty()) {
            return;
        }
        final List<HistoricalMoneyHoldingValue> historicalMoneyHoldingValues = generateHistoricalHoldingValues(userId, accountId, account.getCurrencyId(), transactionsPerTimestamp);

        log.info("Storing {} historical money holding values for account id {}", historicalMoneyHoldingValues.size(), accountId);

        historicalMoneyHoldingValueRepository.deleteByAccountId(accountId);
        historicalMoneyHoldingValueRepository.saveAllAndFlush(historicalMoneyHoldingValues);

        // todo: move to async job, but now we have circular dependency :(
        this.recalculateAggregatedHistoricalHoldingValues(userId);
    }

    /**
     * Calculates the aggregated holding value. It sums up all the amounts and persists it in the database.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recalculateAggregatedHistoricalHoldingValues(final long userId) {
        log.info("Recalculating aggregated historical  values");
        final List<HistoricalMoneyHoldingValue> allHistoricalValues = historicalMoneyHoldingValueRepository.findAll();
        final Map<LocalDate, List<HistoricalMoneyHoldingValue>> historicalValuesByDate = new HashMap<>();
        allHistoricalValues.forEach(historicalSecurityHoldingValue -> {
            historicalValuesByDate.putIfAbsent(historicalSecurityHoldingValue.getDate(), new ArrayList<>());
            historicalValuesByDate.get(historicalSecurityHoldingValue.getDate()).add(historicalSecurityHoldingValue);
        });

        final List<AggregatedHistoricalMoneyHoldingValue> aggregatedValues = historicalValuesByDate
                .values()
                .stream()
                .map(value -> aggregateHoldingValues(userId, value))
                .toList();

        log.info("Storing {} aggregated  holding values", aggregatedValues.size());
        aggregatedHistoricalMoneyHoldingValueRepository.deleteAllByUserId(userId);
        aggregatedHistoricalMoneyHoldingValueRepository.saveAllAndFlush(aggregatedValues);
    }


    private AggregatedHistoricalMoneyHoldingValue aggregateHoldingValues(final long userId, final List<HistoricalMoneyHoldingValue> historicalSecurityHoldingValues) {
        final AggregatedHistoricalMoneyHoldingValue aggregatedValue = new AggregatedHistoricalMoneyHoldingValue(userId);

        for (final HistoricalMoneyHoldingValue historicalSecurityHoldingValue : historicalSecurityHoldingValues) {
            final double currencyMultiplier = historicalSecurityHoldingValue.getCurrencyMultiplier();

            aggregatedValue.setDate(historicalSecurityHoldingValue.getDate());
            aggregatedValue.setAmount(aggregatedValue.getAmount() + (historicalSecurityHoldingValue.getAmount() * currencyMultiplier));
        }

        return aggregatedValue;
    }


    /**
     * This method generates a list of HistoricalHoldingValue that is the complete historical list of the specified holding.
     * It first finds the first timestamp of the transactions and company prices to know where to start. It then loops
     * over the days until now() and creates a new HistoricalHoldingValue for that specific day.
     */
    private List<HistoricalMoneyHoldingValue> generateHistoricalHoldingValues(final long userId,
                                                                              final long accountId,
                                                                              final long currencyId,
                                                                              final Map<LocalDate, List<MoneyTransaction>> transactionsPerTimestamp) {
        final LocalDate firstTimestamp = getFirstTimestamp(transactionsPerTimestamp);

        final List<HistoricalMoneyHoldingValue> historicalHoldingValues = new ArrayList<>();

        HistoricalMoneyHoldingValueDto currentHoldingValue = new HistoricalMoneyHoldingValueDto(accountId, currencyId);

        for (LocalDate currentTimestamp = firstTimestamp;
             currentTimestamp.isBefore(LocalDate.now().plusDays(1));
             currentTimestamp = currentTimestamp.plusDays(1)) {

            final double currencyMultiplier = cacheService.getCurrencyMultiplier(currencyId, currentTimestamp);
            currentHoldingValue.setCurrencyMultiplier(currencyMultiplier);

            currentHoldingValue = calculateHistoricalValue(
                    currentTimestamp,
                    currentHoldingValue,
                    transactionsPerTimestamp.get(currentTimestamp));

            historicalHoldingValues.add(new HistoricalMoneyHoldingValue(userId, currentHoldingValue));
        }
        return historicalHoldingValues;
    }


    /**
     * Calculates the holding value on a specific timestamp, based on the previous value of the holding, current company
     * price and a list of transactions that might be executed that day. It will first process the list of transactions,
     * and then it will calculate the value of the holding. Therefore, the value of the holding is the value of the
     * holding at the end of the day of the timestamp.
     */
    private static HistoricalMoneyHoldingValueDto calculateHistoricalValue(final LocalDate currentTimestamp,
                                                                           final HistoricalMoneyHoldingValueDto previousHoldingValue,
                                                                           final List<MoneyTransaction> transactions) {
        final HistoricalMoneyHoldingValueDto newHoldingValue = new HistoricalMoneyHoldingValueDto(currentTimestamp, previousHoldingValue);

        if (transactions != null) {
            transactions.forEach(transaction -> applyTransaction(newHoldingValue, transaction));
        }

        return newHoldingValue;
    }

    private static void applyTransaction(final HistoricalMoneyHoldingValueDto newHoldingValue, final MoneyTransaction transaction) {
        if (transaction.getFinalBalance() != 0) {
            newHoldingValue.setAmount(transaction.getFinalBalance());
        } else {
            newHoldingValue.setAmount(newHoldingValue.getAmount() + transaction.getAmount());
        }
    }


    /**
     * Returns a map of transactions per date. A single date holds a list of transactions.
     */
    private Map<LocalDate, List<MoneyTransaction>> getTransactionsPerTimestamp(final Long securityId) {
        final Map<LocalDate, List<MoneyTransaction>> transactionsPerTimestamp = new HashMap<>();

        moneyTransactionRepository
                .findByAccountId(securityId)
                .forEach(transaction -> {
                    final LocalDate date = LocalDate.ofInstant(transaction.getTimestamp(), ZoneOffset.UTC);
                    transactionsPerTimestamp.putIfAbsent(date, new ArrayList<>());
                    transactionsPerTimestamp.get(date).add(transaction);
                });
        return transactionsPerTimestamp;
    }

    /**
     * Returns the timestamp of the first transaction
     */
    private static LocalDate getFirstTimestamp(final Map<LocalDate, List<MoneyTransaction>> transactionsPerTimestamp) {
        return transactionsPerTimestamp
                .keySet()
                .stream()
                .sorted()
                .findFirst()
                .orElseThrow();
    }

    @Override
    public boolean canProcess(final Job job) {
        return job.getJobType() == RECALCULATE_HISTORICAL_MONEY_VALUES ||
                job.getJobType() == RECALCULATE_AGGREGATED_HISTORICAL_MONEY_VALUES;
    }

    @Override
    public void process(final Job job) {
        switch (job.getJobType()) {
            case RECALCULATE_HISTORICAL_MONEY_VALUES ->
                    recalculateHistoricalHoldingValues(job.getUserId(), job.getAccountId());
            case RECALCULATE_AGGREGATED_HISTORICAL_MONEY_VALUES ->
                    recalculateAggregatedHistoricalHoldingValues(job.getUserId());
            default -> throw new IllegalStateException();
        }
    }
}
