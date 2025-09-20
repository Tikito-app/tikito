package org.tikito.service.security;

import org.tikito.dto.AccountDto;
import org.tikito.dto.ImportFileType;
import org.tikito.dto.ImportSettings;
import org.tikito.dto.security.*;
import org.tikito.entity.Job;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.entity.security.SecurityHolding;
import org.tikito.entity.security.SecurityTransaction;
import org.tikito.exception.UnsupportedImportFormatException;
import org.tikito.repository.*;
import org.tikito.service.CacheService;
import org.tikito.service.JobService;
import org.tikito.service.importer.FileReader;
import org.tikito.service.importer.security.CustomSecurityTransactionImporter;
import org.tikito.service.importer.security.DeGiroAccountImporter;
import org.tikito.service.importer.security.DeGiroTransactionsImporter;
import org.tikito.service.importer.security.SecurityTransactionImporter;
import org.tikito.service.job.JobType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tikito.dto.security.SecurityTransactionImportResultDto.FAILED_DUPLICATE_TRANSACTION;
import static org.tikito.dto.security.SecurityTransactionImportResultDto.FAILED_NO_EXCHANGE_RATE;
import static org.tikito.dto.security.SecurityTransactionImportResultDto.FAILED_NO_KNOWN_CURRENCY;
import static org.tikito.dto.security.SecurityTransactionImportResultDto.*;

@Service
@Slf4j
public class SecurityTransactionService {
    private final SecurityTransactionRepository securityTransactionRepository;
    private final IsinRepository isinRepository;
    private final SecurityRepository securityRepository;
    private final List<SecurityTransactionImporter> importers;
    private final SecurityHoldingRepository securityHoldingRepository;
    private final JobService jobService;
    private final CacheService cacheService;
    private final AccountRepository accountRepository;

    public SecurityTransactionService(final SecurityTransactionRepository securityTransactionRepository,
                                      final IsinRepository isinRepository,
                                      final SecurityRepository securityRepository,
                                      final DeGiroAccountImporter deGiroAccountImporter,
                                      final DeGiroTransactionsImporter deGiroTransactionsImporter,
                                      final SecurityHoldingRepository securityHoldingRepository,
                                      final JobService jobService,
                                      final CacheService cacheService,
                                      final AccountRepository accountRepository) {
        this.securityTransactionRepository = securityTransactionRepository;
        this.isinRepository = isinRepository;
        this.securityRepository = securityRepository;
        this.securityHoldingRepository = securityHoldingRepository;
        this.jobService = jobService;
        this.cacheService = cacheService;
        this.accountRepository = accountRepository;

        importers = List.of(deGiroAccountImporter, deGiroTransactionsImporter);
    }

    private static boolean hasIsin(final Security security, final String isin) {
        return security
                .getIsins()
                .stream()
                .anyMatch(t -> t.getIsin().equals(isin));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteTransaction(final long userId, final long transactionId) {
        final SecurityTransaction transaction = securityTransactionRepository.findByUserIdAndId(userId, transactionId).orElseThrow();
        securityTransactionRepository.deleteByUserIdAndId(userId, transactionId);
        jobService.addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, transaction.getSecurityId(), userId).build());
        jobService.addJob(Job.security(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userId).build());
    }

    public List<SecurityTransactionDto> getSecurityTransactions(final long userId, final SecurityHoldingFilter filter) {
        final List<SecurityHolding> holdingList = securityHoldingRepository.findByUserIdAndIdIn(userId, filter.getHoldingIds());
        final Set<Long> securityIds = holdingList.stream().map(SecurityHolding::getSecurityId).collect(Collectors.toSet());

        return enrichTransactions(securityTransactionRepository
                .findBySecurityIdIn(securityIds, filter.getStartDateAsInstant())
                .stream()
                .sorted((o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp())));
    }

    /**
     * Enriches the SecurityTransactionDto with the security.
     */
    private List<SecurityTransactionDto> enrichTransactions(final Stream<SecurityTransaction> transactions) {
        return transactions
                .map(SecurityTransaction::toDto)
                .map(transaction -> {
                    transaction.setSecurity(cacheService.getSecurity(transaction.getSecurityId()));
                    return transaction;
                })
                .toList();
    }

    /**
     * Imports a csv file.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public SecurityTransactionImportResultDto importTransactions(final long userId,
                                                                 final ImportSettings importSettings,
                                                                 final Long accountId,
                                                                 final MultipartFile file,
                                                                 final char separatorChar,
                                                                 final char quoteChar,
                                                                 final boolean dryRun,
                                                                 final String customHeaderConfigString,
                                                                 final String buyValue,
                                                                 final String timestampFormat,
                                                                 final String dateFormat,
                                                                 final String timeFormat) throws UnsupportedImportFormatException, IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final TypeReference<HashMap<String, Integer>> typeRef = new TypeReference<>() {
        };

        final HashMap<String, Integer> headerConfig = mapper.readValue(customHeaderConfigString, typeRef);
        return importTransactions(userId, importSettings, accountId, file, separatorChar, quoteChar, dryRun, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
    }

    public SecurityTransactionImportResultDto importTransactions(final long userId,
                                                                 final ImportSettings importSettings,
                                                                 final Long accountId,
                                                                 final MultipartFile file,
                                                                 final char separatorChar,
                                                                 final char quoteChar,
                                                                 final boolean dryRun,
                                                                 final Map<String, Integer> customHeaderConfig,
                                                                 final String buyValue,
                                                                 final String timestampFormat,
                                                                 final String dateFormat,
                                                                 final String timeFormat) throws UnsupportedImportFormatException, IOException {

        final ImportFileType fileType = FileReader.getImportFileType(file);
        final AccountDto account = accountRepository.findById(accountId).orElseThrow().toDto();

        final List<List<String>> lines = fileType == ImportFileType.CSV ?
                FileReader.readCsv(file, separatorChar, quoteChar) :
                FileReader.readExcel(file);

        log.info("Processing file with {} lines for account {}", lines.size(), accountId);

        final SecurityTransactionImportResultDto result = new SecurityTransactionImportResultDto(lines);
        mapCsv(result, customHeaderConfig, buyValue, timestampFormat, dateFormat, timeFormat);

        enrichValidateAndMap(account, importSettings, result);
        failDuplicateTransactions(accountId, result);

        log.info("Found {} new trading companies", result.getNewSecuritiesByIsin().size());

        extractNewHoldingsFromTransactions(userId, accountId, result);
        log.info("Found {} new holdings", result.getNewSecurityHoldings().size());

        final Map<String, Integer> failedAmountsPerReason = new HashMap<>();
        result.getLines().forEach(line -> {
            if (line.isFailed()) {
                log.info("Failed line {} for reason {}: {}", line.getLineNumber(), line.getFailedReason(), line.getCells());
                failedAmountsPerReason.put(line.getFailedReason(), failedAmountsPerReason.getOrDefault(line.getFailedReason(), 0) + 1);
            }
        });

        failedAmountsPerReason.keySet().forEach(reason -> log.info("Failed {}: {}", reason, failedAmountsPerReason.get(reason)));

        log.info("Storing {} new transactions", filterNonFailed(result).count());
        final List<SecurityTransaction> transactions = filterNonFailed(result)
                .map(line -> new SecurityTransaction(userId, accountId, line))
                .toList();

        if (!dryRun) {
            securityRepository.saveAllAndFlush(
                    result
                            .getNewSecuritiesByIsin()
                            .values()
                            .stream()
                            .toList());

            // existing holdings only, because the new ones are added in this map as well
            securityHoldingRepository.saveAll(result.getExistingSecurityHoldings().values());

            result.getImportedTransactions().addAll(securityTransactionRepository
                    .saveAllAndFlush(transactions)
                    .stream()
                    .toList());

            generateJobsAfterImport(userId, result);

            cacheService.refreshSecurities();
        } else {
            result.getImportedTransactions().addAll(transactions);
        }

        return result;
    }

    private void generateJobsAfterImport(final long userId, final SecurityTransactionImportResultDto result) {
        result.getNewSecuritiesByIsin().values().forEach(security -> {
            jobService.addJob(Job.security(JobType.ENRICH_SECURITY, security.getId()).build());
            jobService.addJob(Job.create(JobType.UPDATE_SECURITY_PRICES).securityId(security.getId()).build());
        });

        result.getNewSecurityHoldings().forEach(securityHolding ->
                jobService.addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, securityHolding.getSecurityId(), userId).build()));

        if (!result.getNewSecurityHoldings().isEmpty()) {
            jobService.addJob(Job.create(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES).userId(userId).build());
        }
    }

    /**
     * Sets all the duplicate transactions to failed. Duplicate means that there are more than x amount of
     * the same (holding, security, amount) transactions on the same date.
     */
    private void failDuplicateTransactions(final long accountId, final SecurityTransactionImportResultDto result) {
        final Map<String, List<SecurityTransactionDto>> existingUniqueTransactionsPerDate = new HashMap<>();
        final Map<String, List<SecurityTransactionImportLine>> newUniqueTransactionsPerDate = new HashMap<>();

        // todo: add filter by isin when we don't store them anymore as a comma separated string
        securityTransactionRepository
                .findByAccountId(accountId)
                .forEach(transaction -> {
                    final String uniqueKey = SecurityTransactionDto.getUniqueKey(transaction);
                    existingUniqueTransactionsPerDate.putIfAbsent(uniqueKey, new ArrayList<>());
                    existingUniqueTransactionsPerDate.get(uniqueKey).add(transaction.toDto());
                });
        filterNonFailed(result)
                .forEach(line -> {
                    final String uniqueKey = SecurityTransactionDto.getUniqueKey(accountId, line);
                    newUniqueTransactionsPerDate.putIfAbsent(uniqueKey, new ArrayList<>());
                    newUniqueTransactionsPerDate.get(uniqueKey).add(line);
                });
        newUniqueTransactionsPerDate.forEach((uniqueKey, newList) -> {
            final int existingAmount = existingUniqueTransactionsPerDate.containsKey(uniqueKey) ? existingUniqueTransactionsPerDate.get(uniqueKey).size() : 0;
            final int newAmount = newList.size();

            for (int i = 0; i < existingAmount && i < newAmount; i++) {
                newList.get(i).setFailedReason(FAILED_DUPLICATE_TRANSACTION);
            }
        });
    }

    private Stream<SecurityTransactionImportLine> filterNonFailed(final SecurityTransactionImportResultDto result) {
        return result
                .getLines()
                .stream()
                .filter(line -> !line.isFailed());

    }

    private void extractNewHoldingsFromTransactions(final long userId, final Long accountId, final SecurityTransactionImportResultDto result) {
        result.getExistingSecurityHoldings().putAll(securityHoldingRepository
                .findAll()
                .stream()
                .collect(Collectors.toMap(SecurityHolding::getSecurityId, Function.identity())));
        final Map<Long, SecurityHolding> newHoldingsMap = new HashMap<>();

        // todo write test for filter
        // for each non failed transaction, see if we need to create a new holding, or update the amount on the existing holding
        filterNonFailed(result)
                .filter(transaction ->
                        transaction.getTransactionType() == SecurityTransactionType.BUY ||
                                transaction.getTransactionType() == SecurityTransactionType.SELL)
                .forEach(transaction -> {
                    if (!result.getExistingSecurityHoldings().containsKey(transaction.getSecurityId()) && !newHoldingsMap.containsKey(transaction.getSecurityId())) {
                        final Set<Long> accountIds = new HashSet<>();
                        accountIds.add(accountId);
                        final SecurityHolding securityHolding = new SecurityHolding(userId, accountIds, transaction);
                        newHoldingsMap.put(transaction.getSecurityId(), securityHolding);
                        // also populate the existing one, otherwise we keep on creating new ones
                        // todo: merge the two collectons?
                        result.getExistingSecurityHoldings().put(securityHolding.getSecurityId(), securityHolding);
                    } else {
                        final SecurityHolding securityHolding = result.getExistingSecurityHoldings().get(transaction.getSecurityId());
                        securityHolding.getAccountIds().add(accountId);
                        securityHolding.mutateAmount(transaction);
                    }
                });
        result.getNewSecurityHoldings().addAll(newHoldingsMap.values());
    }

    /**
     * Maps the lines to the SecurityTransactionImportLine based on the found importer. It then validates whether the
     * imported line has all the required.
     */
    private void mapCsv(final SecurityTransactionImportResultDto result, final Map<String, Integer> customHeaderConfig, final String buyValue, final String timestampFormat, final String dateFormat, final String timeFormat) throws UnsupportedImportFormatException {
        final List<String> header = result.getLines().removeFirst().getCells();
        final SecurityTransactionImporter importer = getImporter(header, customHeaderConfig, buyValue, timestampFormat, dateFormat, timeFormat).orElseThrow(UnsupportedImportFormatException::new);
        final List<SecurityTransactionImportLine> addedLines = new ArrayList<>();

        filterNonFailed(result).forEach(line -> {
            addedLines.addAll(importer.map(line));

            // validate the transaction type here, because we need it for the unique key to check for duplicates
            if (!line.isFailed() && line.getTransactionType() == null) {
                line.setFailedReason(FAILED_NO_TRANSACTION_TYPE);
            }
        });

        result.getLines().clear();
        result.getLines().addAll(addedLines);

        // reverse it, because we want to have the old ones first
        Collections.reverse(result.getLines());
    }

    private void enrichValidateAndMap(final AccountDto account, final ImportSettings importSettings, final SecurityTransactionImportResultDto result) {
        final Set<String> isinSet = filterNonFailed(result).map(SecurityTransactionImportLine::getIsin).collect(Collectors.toSet());
        final Map<String, Isin> knownIsins = new HashMap<>();
        final Map<String, Security> knownSecuritiesByIsin = new HashMap<>();

        isinRepository.findAllById(isinSet).forEach(isin -> {
            knownIsins.put(isin.getIsin(), isin);
            knownSecuritiesByIsin.put(isin.getIsin(), isin.getSecurity());
        });

        // todo: create new job to enrich the trading companies
        final List<SecurityTransactionImportLine> lines = filterNonFailed(result).toList();

        for (int i = 0; i < lines.size(); i++) {
            enrichValidateAndMap(account, importSettings, knownIsins, knownSecuritiesByIsin, result.getNewSecuritiesByIsin(), lines, i);
        }
    }

    /**
     * Validates for valid currency. In case an isin is present,
     */
    void enrichValidateAndMap(final AccountDto account,
                              final ImportSettings importSettings,
                              final Map<String, Isin> knownIsins,
                              final Map<String, Security> knownSecuritiesByIsin,
                              final Map<String, Security> newSecuritiesByIsin,
                              final List<SecurityTransactionImportLine> lines,
                              final int currentIndex) {
        final SecurityTransactionImportLine line = lines.get(currentIndex);
        if (line.isFailed()) {
            return;
        }

        if (StringUtils.hasText(line.getCurrency())) {
            final Optional<SecurityDto> currency = cacheService.getCurrency(line.getCurrency(), line.getTimestamp());
            if (currency.isEmpty()) {
                line.setFailedReason(FAILED_NO_KNOWN_CURRENCY);
                return;
            }
            line.setCurrencyId(currency.get().getId());
        } else {
            line.setCurrencyId(account.getCurrencyId());
        }

        if (line.getExchangeRate() == null) {
            line.setExchangeRate(cacheService.getCurrencyMultiplier(line.getCurrencyId(), LocalDate.ofInstant(line.getTimestamp(), ZoneOffset.UTC)));
        }

        if (line.getExchangeRate() == null) {
            line.setFailedReason(FAILED_NO_EXCHANGE_RATE);
            return;
        }


        if (StringUtils.hasText(line.getIsin())) {
            Security security = new Security(line.getIsin());
            security.setSecurityType(SecurityType.STOCK);
            security.setName(line.getProductName());
            security.setCurrencyId(line.getCurrencyId());
            if (!StringUtils.hasText(security.getName())) {
                security.setName(line.getIsin());
            }

            if (line.getTransactionType() == SecurityTransactionType.SELL_ISIN_CHANGE) {
                // in this case, the new isin is on the next line, so we need to validate that that next line exists
                if (currentIndex >= lines.size() - 1) {
                    line.setFailedReason(FAILED_EXPECTED_BUY_ISIN_CHANGE);
                    return;
                }

                final SecurityTransactionImportLine nextLine = lines.get(currentIndex + 1);

                if (!line.getTimestamp().equals(nextLine.getTimestamp())) {
                    line.setFailedReason(FAILED_EXPECTED_BUY_NEW_ISIN_SAME_TIMESTAMP);
                } else if (nextLine.getTransactionType() != SecurityTransactionType.BUY_ISIN_CHANGE) {
                    line.setFailedReason(FAILED_EXPECTED_BUY_ISIN_CHANGE);
                    return;
                }

                security = knownSecuritiesByIsin.get(line.getIsin());

                final boolean isinAlreadyPresent = hasIsin(security, nextLine.getIsin());

                if (!isinAlreadyPresent) {
                    final Isin newIsinDto = security.onNewIsin(LocalDate.ofInstant(line.getTimestamp(), ZoneOffset.UTC), line.getIsin(), nextLine.getIsin());
                    knownIsins.put(nextLine.getIsin(), newIsinDto);
                    knownSecuritiesByIsin.put(nextLine.getIsin(), security);

                    securityRepository.saveAndFlush(security);
                }
            } else if (knownIsins.containsKey(line.getIsin())) {
                security = knownSecuritiesByIsin.get(knownIsins.get(line.getIsin()).getIsin());
            } else if (!importSettings.isCanImportNewIsin()) {
                line.setFailedReason("No allowed to create new trading company isin");
                return;
            } else {
                log.warn("Accepting unknown isin {} to company {}", line.getIsin(), security.getId());
                newSecuritiesByIsin.put(line.getIsin(), security);
                knownIsins.put(line.getIsin(), security.getIsins().getFirst());
                knownSecuritiesByIsin.put(line.getIsin(), security);
            }

            security = securityRepository.saveAndFlush(security);
            line.setSecurityId(security.getId());
        }
    }

    /**
     * Find the appropiate importer for the given csv file, based on the header files.
     * <p>
     * todo: add more identification validations, such as based on filenames.
     */
    private Optional<SecurityTransactionImporter> getImporter(final List<String> header,
                                                              final Map<String, Integer> customHeaderConfig,
                                                              final String buyValue,
                                                              final String timestampFormat,
                                                              final String dateFormat,
                                                              final String timeFormat) {
        if (customHeaderConfig != null) {
            return Optional.of(new CustomSecurityTransactionImporter(customHeaderConfig, buyValue, timestampFormat, dateFormat, timeFormat));
        }

        for (final SecurityTransactionImporter importer : importers) {
            if (importer.matchesHeader(header)) {
                return Optional.of(importer);
            }
        }
        return Optional.empty();
    }
}
