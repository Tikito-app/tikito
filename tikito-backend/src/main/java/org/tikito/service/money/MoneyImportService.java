package org.tikito.service.money;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.tikito.dto.AccountDto;
import org.tikito.dto.ImportFileType;
import org.tikito.dto.money.MoneyTransactionDto;
import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.dto.security.SecurityDto;
import org.tikito.entity.Job;
import org.tikito.entity.money.MoneyHolding;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.exception.CannotReadFileException;
import org.tikito.repository.AccountRepository;
import org.tikito.repository.MoneyHoldingRepository;
import org.tikito.repository.MoneyTransactionRepository;
import org.tikito.service.CacheService;
import org.tikito.service.JobFactoryService;
import org.tikito.service.MT940.MT940Parser;
import org.tikito.service.importer.FileReader;
import org.tikito.service.importer.money.*;
import org.tikito.service.job.JobType;
import org.tikito.util.Util;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tikito.dto.money.MoneyTransactionImportResultDto.*;

@Service
@Slf4j
public class MoneyImportService {

    private final MoneyTransactionRepository moneyTransactionRepository;
    private final List<MoneyTransactionFileParser> fileParsers;
    private final List<MoneyTransactionImporter> importers;
    private final AccountRepository accountRepository;
    private final CacheService cacheService;
    private final JobFactoryService jobFactorService;
    private final MoneyHoldingRepository moneyHoldingRepository;

    public MoneyImportService(final MoneyTransactionRepository moneyTransactionRepository,
                              final ABNFileParser abnFileParser,
                              final INGFileParser ingFileParser,
                              final BunqFileParser bunqFileParser,
                              final BitvavoFileParser bitvavoFileParser,
                              final List<MoneyTransactionImporter> importers,
                              final AccountRepository accountRepository,
                              final CacheService cacheService,
                              final JobFactoryService jobFactorService,
                              final MoneyHoldingRepository moneyHoldingRepository) {
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.moneyHoldingRepository = moneyHoldingRepository;
        this.fileParsers = List.of(abnFileParser, ingFileParser, bitvavoFileParser, bunqFileParser);
        this.importers = new ArrayList<>(importers);
        this.accountRepository = accountRepository;
        this.cacheService = cacheService;
        this.jobFactorService = jobFactorService;
    }

    public MoneyTransactionImportResultDto importTransactions(final long userId,
                                                              final long accountId,
                                                              final MultipartFile file,
                                                              final boolean dryRun,
                                                              final String customHeaderConfigString,
                                                              final String debitIndication,
                                                              final String timestampFormat,
                                                              final String timeFormat,
                                                              final String csvSeparator) throws CannotReadFileException, JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final TypeReference<HashMap<String, Integer>> typeRef = new TypeReference<>() {
        };

        final HashMap<String, Integer> headerConfig = mapper.readValue(customHeaderConfigString, typeRef);
        return importTransactions(userId, accountId, file, dryRun, headerConfig, debitIndication, timestampFormat, timeFormat, csvSeparator);
    }

    public MoneyTransactionImportResultDto importTransactions(final long userId,
                                                              final long accountId,
                                                              final MultipartFile file,
                                                              final boolean dryRun,
                                                              final Map<String, Integer> customHeaderConfig,
                                                              final String debitIndication,
                                                              final String timestampFormat,
                                                              final String timeFormat,
                                                              final String csvSeparator) throws CannotReadFileException {
        try {
            final ImportFileType importFileType = FileReader.getImportFileType(file);
            final AccountDto account = accountRepository.findByUserIdAndId(userId, accountId).orElseThrow().toDto();

            if (importFileType == ImportFileType.MT940) {
                return importTransactionsFromMT940(account, file, dryRun);
            } else if (importFileType == ImportFileType.CSV) {
                return importTransactionsFromCsv(account, file, dryRun, getSeparator(csvSeparator), '"', customHeaderConfig, debitIndication, timestampFormat, timeFormat);
            } else if (importFileType == ImportFileType.EXCEL) {
                return importTransactionsFromExcel(account, file, dryRun, customHeaderConfig, debitIndication, timestampFormat, timeFormat);
            }
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new CannotReadFileException();
        }
        return null;
    }

    private MoneyTransactionImportResultDto importTransactionsFromMT940(final AccountDto account, final MultipartFile file, final boolean dryRun) {
        try {
            final MoneyTransactionImportResultDto result = new MoneyTransactionImportResultDto(account, MT940Parser.parse(new String(file.getBytes())), file.getOriginalFilename());
            return importTransactions(account, result, dryRun);
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private MoneyTransactionImportResultDto importTransactionsFromCsv(final AccountDto account,
                                                                      final MultipartFile file,
                                                                      final boolean dryRun,
                                                                      final char separatorChar,
                                                                      final char quoteChar,
                                                                      final Map<String, Integer> customHeaderConfig,
                                                                      final String debitIndication,
                                                                      final String timestampFormat,
                                                                      final String timeFormat) throws CannotReadFileException {
        final List<List<String>> csv = FileReader.readCsv(file, separatorChar, quoteChar);
        return importTransactionsFromParsedFileLines(account, file, csv, dryRun, customHeaderConfig, debitIndication, timestampFormat, timeFormat);
    }

    private MoneyTransactionImportResultDto importTransactionsFromExcel(final AccountDto account,
                                                                        final MultipartFile file,
                                                                        final boolean dryRun,
                                                                        final Map<String, Integer> customHeaderConfig,
                                                                        final String debitIndication,
                                                                        final String timestampFormat,
                                                                        final String timeFormat) throws IOException {
        final List<List<String>> csv = FileReader.readExcel(file.getInputStream(), Util.getFileExtension(file.getOriginalFilename()));
        return importTransactionsFromParsedFileLines(account, file, csv, dryRun, customHeaderConfig, debitIndication, timestampFormat, timeFormat);
    }

    private MoneyTransactionImportResultDto importTransactionsFromParsedFileLines(final AccountDto account,
                                                                                  final MultipartFile file,
                                                                                  final List<List<String>> lines,
                                                                                  final boolean dryRun,
                                                                                  final Map<String, Integer> customHeaderConfig,
                                                                                  final String debitIndication,
                                                                                  final String timestampFormat,
                                                                                  final String timeFormat) throws CannotReadFileException {
        final MoneyTransactionFileParser importer =
                customHeaderConfig != null && !customHeaderConfig.isEmpty() ?
                        new CustomMoneyFileParser(customHeaderConfig, debitIndication, timestampFormat, timeFormat) :
                        fileParsers.stream()
                        .filter(i -> i.matchesHeader(lines.getFirst()))
                        .findAny()
                        .orElseThrow(CannotReadFileException::new);// todo
        lines.removeFirst(); // remove headers

        final MoneyTransactionImportSettings settings = importer.getSettings();
        final MoneyTransactionImportResultDto result = MoneySettingsService.applySettings(settings, lines, importer, file.getOriginalFilename());

        return importTransactions(account, result, dryRun);
    }

    public MoneyTransactionImportResultDto importTransactions(final AccountDto account, final MoneyTransactionImportResultDto result, final boolean dryRun) {
        importers.stream()
                .filter(importer -> importer.applies(result))
                .findAny()
                .ifPresent(importer -> importer.apply(result));

        enrichValidateAndMap(account, result);
        failDuplicates(account.getId(), result);

        final Map<String, Integer> failedAmountsPerReason = new HashMap<>();
        result.getLines().forEach(line -> {
            if (line.isFailed()) {
                failedAmountsPerReason.put(line.getFailedReason(), failedAmountsPerReason.getOrDefault(line.getFailedReason(), 0) + 1);
            }
        });

        failedAmountsPerReason.keySet().forEach(reason -> log.info("Failed {}: {}", reason, failedAmountsPerReason.get(reason)));

        log.info("Storing {} new money transactions", filterNonFailed(result).count());
        final List<MoneyTransaction> transactions = filterNonFailed(result)
                .map(line -> new MoneyTransaction(account.getUserId(), account.getId(), line))
                .toList();

        if (!dryRun) {
            assertMoneyHoldings(account.getUserId(), account.getId(), transactions);
            result.getImportedTransactions().addAll(moneyTransactionRepository
                    .saveAllAndFlush(transactions)
                    .stream()
                    .toList());
            generateJobsAfterImport(account.getUserId(), account.getId(), result);
        } else {
            result.getImportedTransactions().addAll(transactions);
        }

        return result;
    }

    /**
     * For money we need to create a money holding per currency.
     */
    private void assertMoneyHoldings(final long userId, final long accountId, final List<MoneyTransaction> transactionsToImport) {
        final Map<Long, MoneyHolding> existingHoldingsPerCurrencyId = moneyHoldingRepository.findByUserIdAndAccountId(userId, accountId)
                .stream()
                .collect(Collectors.toMap(
                        MoneyHolding::getCurrencyId,
                        Function.identity()
                ));
        final List<MoneyHolding> newHoldings = new ArrayList<>();

        transactionsToImport.forEach(transaction -> {
            if (!existingHoldingsPerCurrencyId.containsKey(transaction.getCurrencyId())) {
                final MoneyHolding holding = new MoneyHolding();
                holding.setUserId(userId);
                holding.setAccountId(accountId);
                holding.setCurrencyId(transaction.getCurrencyId());
                newHoldings.add(holding);
                existingHoldingsPerCurrencyId.put(transaction.getCurrencyId(), holding);
            }
        });
        moneyHoldingRepository.saveAllAndFlush(newHoldings);
    }

    private void generateJobsAfterImport(final long userId, final long accountId, final MoneyTransactionImportResultDto result) {
        if (!result.getImportedTransactions().isEmpty()) {
            jobFactorService.addJob(Job.account(JobType.RECALCULATE_HISTORICAL_MONEY_VALUES, accountId, userId).build());
            jobFactorService.addJob(Job.account(JobType.GROUP_MONEY_TRANSACTIONS, accountId, userId).build());
            jobFactorService.addJob(Job.create(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES).userId(userId).build());
        }
    }

    private void enrichValidateAndMap(final AccountDto accountDto, final MoneyTransactionImportResultDto result) {
        final List<MoneyTransactionImportLine> lines = filterNonFailed(result).toList();

        for (int i = 0; i < lines.size(); i++) {
            enrichValidateAndMap(accountDto, lines, i);
        }
    }

    // visible for testing
    void enrichValidateAndMap(final AccountDto accountDto, final List<MoneyTransactionImportLine> lines, final int currentIndex) {
        final MoneyTransactionImportLine line = lines.get(currentIndex);
        if (StringUtils.hasText(line.getCurrency())) {
            final Optional<SecurityDto> currency = cacheService.getCurrency(line.getCurrency(), line.getTimestamp());
            if (currency.isEmpty()) {
                line.setFailedReason(FAILED_NO_KNOWN_CURRENCY);
                return;
            }
            line.setCurrencyId(currency.get().getId());
        } else {
            line.setCurrencyId(accountDto.getCurrencyId());
        }

        if (line.getExchangeRate() == null) {
            line.setExchangeRate(cacheService.getCurrencyMultiplier(line.getCurrencyId(), LocalDate.ofInstant(line.getTimestamp(), ZoneOffset.UTC)));
        }

        if (line.getExchangeRate() == null) {
            line.setFailedReason(FAILED_NO_EXCHANGE_RATE);
            return;
        }

        if (line.getTimestamp() == null) {
            line.setFailedReason(FAILED_NO_VALID_TIMESTAMP);
            return;
        }
        if (StringUtils.hasText(line.getCounterpartyAccountNumber())) {
            line.setCounterpartyAccountNumber(line.getCounterpartyAccountNumber().replace(" ", ""));
        }
    }

    private void failDuplicates(final long accountId, final MoneyTransactionImportResultDto result) {
        final Map<String, List<MoneyTransactionDto>> existingUniqueTransactionsPerDate = new HashMap<>();
        final Map<String, List<MoneyTransactionImportLine>> newUniqueTransactionsPerDate = new HashMap<>();

        moneyTransactionRepository
                .findByAccountId(accountId)
                .forEach(transaction -> {
                    final String uniqueKey = MoneyTransactionDto.getUniqueKey(transaction);
                    existingUniqueTransactionsPerDate.putIfAbsent(uniqueKey, new ArrayList<>());
                    existingUniqueTransactionsPerDate.get(uniqueKey).add(transaction.toDto());
                });
        filterNonFailed(result)
                .forEach(line -> {
                    final String uniqueKey = MoneyTransactionDto.getUniqueKey(line);
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


    private Stream<MoneyTransactionImportLine> filterNonFailed(final MoneyTransactionImportResultDto result) {
        return result
                .getLines()
                .stream()
                .filter(line -> !line.isFailed());

    }

    private char getSeparator(final String csvSeparator) {
        return StringUtils.hasText(csvSeparator) ? csvSeparator.charAt(0) : ';';
    }
}
