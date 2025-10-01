package org.tikito.service.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tikito.dto.AccountDto;
import org.tikito.dto.ImportFileType;
import org.tikito.dto.ImportSettings;
import org.tikito.dto.security.SecurityTransactionDto;
import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionImportResultDto;
import org.tikito.dto.security.SecurityTransactionType;
import org.tikito.entity.Job;
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

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tikito.dto.security.SecurityTransactionImportResultDto.FAILED_DUPLICATE_TRANSACTION;
import static org.tikito.dto.security.SecurityTransactionImportResultDto.FAILED_NO_TRANSACTION_TYPE;

@Service
@Slf4j
public class SecurityImportService {
    private final SecurityTransactionRepository securityTransactionRepository;
    private final SecurityRepository securityRepository;
    private final IsinRepository isinRepository;
    private final List<SecurityTransactionImporter> importers;
    private final SecurityHoldingRepository securityHoldingRepository;
    private final JobService jobService;
    private final CacheService cacheService;
    private final AccountRepository accountRepository;
    private final SecurityIsinMappingService securityIsinMappingService;

    public SecurityImportService(final SecurityTransactionRepository securityTransactionRepository,
                                 final SecurityRepository securityRepository,
                                 final IsinRepository isinRepository,
                                 final DeGiroAccountImporter deGiroAccountImporter,
                                 final DeGiroTransactionsImporter deGiroTransactionsImporter,
                                 final SecurityHoldingRepository securityHoldingRepository,
                                 final JobService jobService,
                                 final CacheService cacheService,
                                 final AccountRepository accountRepository,
                                 final SecurityIsinMappingService securityIsinMappingService) {
        this.securityTransactionRepository = securityTransactionRepository;
        this.securityRepository = securityRepository;
        this.isinRepository = isinRepository;
        this.securityHoldingRepository = securityHoldingRepository;
        this.jobService = jobService;
        this.cacheService = cacheService;
        this.accountRepository = accountRepository;
        this.securityIsinMappingService = securityIsinMappingService;

        importers = List.of(deGiroAccountImporter, deGiroTransactionsImporter);
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

        return importTransactions(account, result, dryRun);
    }

    public SecurityTransactionImportResultDto importTransactions(final AccountDto account,
                                                                 final SecurityTransactionImportResultDto result,
                                                                 final boolean dryRun) {

        securityIsinMappingService.enrichValidateAndMap(account, result);
        failDuplicateTransactions(account.getId(), result);

        log.info("Found {} new trading companies", result.getNewSecuritiesByIsin().size());

        final Map<String, Integer> failedAmountsPerReason = new HashMap<>();
        result.getLines().forEach(line -> {
            if (line.isFailed()) {
                log.info("Failed line {} for reason {}: {}", line.getLineNumber(), line.getFailedReason(), line.getCells());
                failedAmountsPerReason.put(line.getFailedReason(), failedAmountsPerReason.getOrDefault(line.getFailedReason(), 0) + 1);
            }
        });

        failedAmountsPerReason.keySet().forEach(reason -> log.info("Failed {}: {}", reason, failedAmountsPerReason.get(reason)));


        if (!dryRun) {
            // first save the new securities
            securityRepository.saveAllAndFlush(result.getNewSecuritiesByIsin().values());

            // then link the new isins to the securities
            result.getNewIsinsByIsin()
                    .values()
                    .forEach(isin -> isin.setSecurityId(result.getNewSecuritiesByIsin().get(isin.getIsin()).getId()));
            isinRepository.saveAllAndFlush(result.getNewIsinsByIsin().values());

            // we can only extract new holdings once the securities are saved, because the holding needs the security id
            extractNewHoldingsFromTransactions(account.getUserId(), account.getId(), result);
            log.info("Found {} new holdings", result.getNewSecurityHoldings().size());

            // existing holdings only, because the new ones are added in this map as well
            securityHoldingRepository.saveAll(result.getExistingSecurityHoldings().values());

            // now the security objects in the lines are filled with their id, so we can create the transactions
            log.info("Storing {} new transactions", filterNonFailed(result).count());
            final List<SecurityTransaction> transactions = filterNonFailed(result)
                    .map(line -> new SecurityTransaction(account.getUserId(), account.getId(), line))
                    .toList();

            result.getImportedTransactions().addAll(securityTransactionRepository
                    .saveAllAndFlush(transactions)
                    .stream()
                    .toList());

            generateJobsAfterImport(account.getUserId(), result);

            cacheService.refreshSecurities();
        } else {

            log.info("Storing {} new transactions", filterNonFailed(result).count());
            final List<SecurityTransaction> transactions = filterNonFailed(result)
                    .map(line -> new SecurityTransaction(account.getUserId(), account.getId(), line))
                    .toList();
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
                    if (!result.getExistingSecurityHoldings().containsKey(transaction.getSecurity().getId()) && !newHoldingsMap.containsKey(transaction.getSecurity().getId())) {
                        final Set<Long> accountIds = new HashSet<>();
                        accountIds.add(accountId);
                        final SecurityHolding securityHolding = new SecurityHolding(userId, accountIds, transaction);
                        newHoldingsMap.put(transaction.getSecurity().getId(), securityHolding);
                        // also populate the existing one, otherwise we keep on creating new ones
                        // todo: merge the two collectons?
                        result.getExistingSecurityHoldings().put(securityHolding.getSecurityId(), securityHolding);
                    } else {
                        final SecurityHolding securityHolding = result.getExistingSecurityHoldings().get(transaction.getSecurity().getId());
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

    private Stream<SecurityTransactionImportLine> filterNonFailed(final SecurityTransactionImportResultDto result) {
        return result
                .getLines()
                .stream()
                .filter(line -> !line.isFailed());
    }
}
