package org.tikito.service.security;

import org.tikito.dto.security.AggregatedHistoricalSecurityHoldingValueDto;
import org.tikito.dto.security.HistoricalSecurityHoldingValueDto;
import org.tikito.dto.security.SecurityHoldingDto;
import org.tikito.dto.security.SecurityHoldingFilter;
import org.tikito.entity.Job;
import org.tikito.entity.security.*;
import org.tikito.repository.*;
import org.tikito.service.CacheService;
import org.tikito.service.job.JobProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.tikito.service.job.JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES;
import static org.tikito.service.job.JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES;

@Slf4j
@Service
public class SecurityHoldingService implements JobProcessor {
    private final SecurityHoldingRepository securityHoldingRepository;
    private final HistoricalSecurityHoldingValueRepository historicalSecurityHoldingValueRepository;
    private final SecurityRepository securityRepository;
    private final SecurityPriceRepository securityPriceRepository;
    private final SecurityTransactionRepository securityTransactionRepository;
    private final CacheService cacheService;
    private final AggregatedHistoricalSecurityHoldingValueRepository aggregatedHistoricalSecurityHoldingValueRepository;

    public SecurityHoldingService(final SecurityHoldingRepository securityHoldingRepository,
                                  final HistoricalSecurityHoldingValueRepository historicalSecurityHoldingValueRepository,
                                  final SecurityRepository securityRepository,
                                  final SecurityPriceRepository securityPriceRepository,
                                  final SecurityTransactionRepository securityTransactionRepository,
                                  final CacheService cacheService,
                                  final AggregatedHistoricalSecurityHoldingValueRepository aggregatedHistoricalSecurityHoldingValueRepository) {
        this.securityHoldingRepository = securityHoldingRepository;
        this.historicalSecurityHoldingValueRepository = historicalSecurityHoldingValueRepository;
        this.securityRepository = securityRepository;
        this.securityPriceRepository = securityPriceRepository;
        this.securityTransactionRepository = securityTransactionRepository;
        this.cacheService = cacheService;
        this.aggregatedHistoricalSecurityHoldingValueRepository = aggregatedHistoricalSecurityHoldingValueRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteSecurityHolding(final long userId, final long securityHoldingId) {
        final Optional<SecurityHolding> maybeSecurityHolding = securityHoldingRepository.findByUserIdAndId(userId, securityHoldingId);
        maybeSecurityHolding.ifPresent((securityHolding -> {
            historicalSecurityHoldingValueRepository.deleteAllBySecurityHoldingId(securityHoldingId);
            securityHoldingRepository.deleteById(securityHoldingId);
            securityTransactionRepository.deleteByUserIdAndSecurityId(userId, securityHolding.getSecurityId());
        }));
        recalculateAggregatedHistoricalHoldingValues(userId);
    }

    /**
     * This method generates a list of HistoricalHoldingValue that is the complete historical list of the specified holding.
     * It first finds the first timestamp of the transactions and company prices to know where to start. It then loops
     * over the days until now() and creates a new HistoricalHoldingValue for that specific day.
     */
    private List<HistoricalSecurityHoldingValue> generateHistoricalHoldingValues(final long userId,
                                                                                 final Set<Long> accountIds,
                                                                                 final Long securityId,
                                                                                 final Long securityHoldingId,
                                                                                 final long currencyId,
                                                                                 final Map<LocalDate, SecurityPrice> companyPricePerTimestamp,
                                                                                 final Map<LocalDate, List<SecurityTransaction>> transactionsPerTimestamp) {
        final LocalDate firstTimestamp = getFirstTimestamp(transactionsPerTimestamp);
        final List<HistoricalSecurityHoldingValue> historicalSecurityHoldingValues = new ArrayList<>();

        HistoricalSecurityHoldingValueDto currentHoldingValue = new HistoricalSecurityHoldingValueDto(accountIds, securityId, securityHoldingId, currencyId);

        for (LocalDate currentTimestamp = firstTimestamp;
             currentTimestamp.isBefore(LocalDate.now().plusDays(1));
             currentTimestamp = currentTimestamp.plusDays(1)) {

            final double currencyMultiplier = cacheService.getCurrencyMultiplier(currencyId, currentTimestamp);
            currentHoldingValue.setCurrencyMultiplier(currencyMultiplier);

            currentHoldingValue = SecurityCalculator.calculateHistoricalValue(
                    currentTimestamp,
                    currentHoldingValue,
                    companyPricePerTimestamp.get(currentTimestamp),
                    transactionsPerTimestamp.get(currentTimestamp));

            historicalSecurityHoldingValues.add(new HistoricalSecurityHoldingValue(userId, currentHoldingValue));
        }
        return historicalSecurityHoldingValues;
    }

    /**
     * Returns the timestamp of the first transaction
     */
    private static LocalDate getFirstTimestamp(final Map<LocalDate, List<SecurityTransaction>> transactionsPerTimestamp) {
        return transactionsPerTimestamp
                .keySet()
                .stream()
                .sorted()
                .findFirst()
                .orElseThrow();
    }

    public List<SecurityHoldingDto> getSecurityHoldings(final long userId) {
        return securityHoldingRepository
                .findByUserId(userId)
                .stream()
                .map(SecurityHolding::toDto)
                .map(holding -> {
                    holding.setSecurity(cacheService.getSecurity(holding.getSecurityId()));
                    return holding;
                })
                .sorted(Comparator.comparing(o -> o.getSecurity().getName()))
                .toList();
    }

    public SecurityHoldingDto getSecurityHolding(final long userId, final long securityHoldingId) {
        return securityHoldingRepository
                .findByUserIdAndId(userId, securityHoldingId)
                .map(holding -> holding.toDto(cacheService.getSecurity(holding.getSecurityId())))
                .orElseThrow();
    }

    public List<HistoricalSecurityHoldingValueDto> getHistoricalHoldingValues(final long userId, final SecurityHoldingFilter filter) {
        return historicalSecurityHoldingValueRepository
                .findAllBySecurityHoldingIdIn(userId, filter.getHoldingIds(), filter.getStartDate())
                .stream()
                .map(HistoricalSecurityHoldingValue::toDto)
                .sorted(Comparator.comparing(HistoricalSecurityHoldingValueDto::getDate))
                .toList();
    }

    public List<AggregatedHistoricalSecurityHoldingValueDto> getAggregatedHistoricalHoldingValues(final long userId) {
        return aggregatedHistoricalSecurityHoldingValueRepository
                .findByUserId(userId)
                .stream()
                .map(AggregatedHistoricalSecurityHoldingValue::toDto)
                .sorted(Comparator.comparing(AggregatedHistoricalSecurityHoldingValueDto::getDate))
                .toList();
    }

    public List<HistoricalSecurityHoldingValueDto> getHistoricalHoldingValues(final long userId, final Set<Long> ids) {
        return historicalSecurityHoldingValueRepository
                .findAllBySecurityHoldingIdIn(userId, ids, null)
                .stream()
                .map(HistoricalSecurityHoldingValue::toDto)
                .sorted(Comparator.comparing(HistoricalSecurityHoldingValueDto::getDate))
                .toList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recalculateHistoricalValue(final long userId, final long securityId) {
        securityHoldingRepository
                .findByUserIdAndSecurityId(userId, securityId)
                .forEach(holding -> recalculateHistoricalHoldingValue(userId, holding.getId()));
    }

    /**
     * This method deletes the previous holding values, generates a completely new list of historical values for that
     * holding and then persists it in the database,
     */
    private void recalculateHistoricalHoldingValue(final long userId, final Long securityHoldingId) {
        log.info("Recalculating historical security holding values for {}", securityHoldingId);

        final SecurityHolding holding = securityHoldingRepository.findByUserIdAndId(userId, securityHoldingId).orElseThrow();
        final Security security = securityRepository.findById(holding.getSecurityId()).orElseThrow();
        final Map<LocalDate, List<SecurityTransaction>> transactionsPerTimestamp = getTransactionsPerTimestamp(holding.getSecurityId());
        final Map<LocalDate, SecurityPrice> companyPricePerTimestamp = securityPriceRepository
                .findAllBySecurityId(security.getId())
                .stream()
                .collect(Collectors.toMap(SecurityPrice::getDate, Function.identity()));
        final List<HistoricalSecurityHoldingValue> historicalSecurityHoldingValues = generateHistoricalHoldingValues(
                userId,
                holding.getAccountIds(),
                holding.getSecurityId(),
                holding.getId(),
                security.getCurrencyId(),
                companyPricePerTimestamp,
                transactionsPerTimestamp);

        log.info("Storing {} historical security holding values for {}", historicalSecurityHoldingValues.size(), securityHoldingId);

        if (!historicalSecurityHoldingValues.isEmpty()) {
            final HistoricalSecurityHoldingValue latestValue = historicalSecurityHoldingValues.getLast();
            final Optional<SecurityHolding> optionalHolding = securityHoldingRepository.findById(latestValue.getSecurityHoldingId());
            if (optionalHolding.isPresent()) {
                final SecurityHolding securityHolding = optionalHolding.get();
                securityHolding.setAmount(latestValue.getAmount());
                securityHoldingRepository.saveAndFlush(securityHolding);
            } else {
                log.error("No security holding present for id {}", latestValue.getSecurityHoldingId());
            }
        }

        if (!historicalSecurityHoldingValues.isEmpty()) {
            holding.apply(historicalSecurityHoldingValues.getLast());
        }
        historicalSecurityHoldingValueRepository.deleteAllBySecurityHoldingId(securityHoldingId);
        historicalSecurityHoldingValueRepository.saveAllAndFlush(historicalSecurityHoldingValues);
        securityHoldingRepository.saveAndFlush(holding);

        // todo: remove and make a job, so that we don't calculate it unnecessary.
        recalculateAggregatedHistoricalHoldingValues(userId);
    }

    /**
     * Calculates the aggregated holding value. It sums up all the values and persists it in the database.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void recalculateAggregatedHistoricalHoldingValues(final long userId) {
        log.info("Recalculating aggregated historical security values");
        final List<HistoricalSecurityHoldingValue> allHistoricalValues = historicalSecurityHoldingValueRepository.findAll();
        final Map<LocalDate, List<HistoricalSecurityHoldingValue>> historicalValuesByDate = new HashMap<>();
        allHistoricalValues.forEach(historicalSecurityHoldingValue -> {
            historicalValuesByDate.putIfAbsent(historicalSecurityHoldingValue.getDate(), new ArrayList<>());
            historicalValuesByDate.get(historicalSecurityHoldingValue.getDate()).add(historicalSecurityHoldingValue);
        });

        final List<AggregatedHistoricalSecurityHoldingValue> aggregatedValues = historicalValuesByDate
                .values()
                .stream()
                .map(value -> SecurityCalculator.aggregateHoldingValues(userId, value))
                .toList();

        log.info("Storing {} aggregated holding values", aggregatedValues.size());
        aggregatedHistoricalSecurityHoldingValueRepository.deleteAllByUserId(userId);
        aggregatedHistoricalSecurityHoldingValueRepository.saveAllAndFlush(aggregatedValues);
    }

    /**
     * Returns a map of transactions per date. A single date holds a list of transactions.
     */
    private Map<LocalDate, List<SecurityTransaction>> getTransactionsPerTimestamp(final Long securityId) {
        final Map<LocalDate, List<SecurityTransaction>> transactionsPerTimestamp = new HashMap<>();

        securityTransactionRepository
                .findBySecurityId(securityId)
                .forEach(transaction -> {
                    final LocalDate date = LocalDate.ofInstant(transaction.getTimestamp(), ZoneOffset.UTC);
                    transactionsPerTimestamp.putIfAbsent(date, new ArrayList<>());
                    transactionsPerTimestamp.get(date).add(transaction);
                });
        return transactionsPerTimestamp;
    }

    @Override
    public boolean canProcess(final Job job) {
        return job.getJobType() == RECALCULATE_HISTORICAL_SECURITY_VALUES ||
                job.getJobType() == RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES;
    }

    @Override
    public void process(final Job job) {
        switch (job.getJobType()) {
            case RECALCULATE_HISTORICAL_SECURITY_VALUES ->
                    recalculateHistoricalValue(job.getUserId(), job.getSecurityId());
            case RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES ->
                    recalculateAggregatedHistoricalHoldingValues(job.getUserId());
            default -> throw new IllegalStateException();
        }
    }
}
