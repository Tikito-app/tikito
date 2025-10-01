package org.tikito.service.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tikito.dto.ImportSettings;
import org.tikito.dto.export.AccountExportDto;
import org.tikito.dto.export.MoneyTransactionGroupExportDto;
import org.tikito.dto.export.TikitoExportDto;
import org.tikito.dto.security.SecurityTransactionImportResultDto;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.Account;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.security.Security;
import org.tikito.entity.security.SecurityTransaction;
import org.tikito.repository.*;
import org.tikito.service.money.MoneyImportService;
import org.tikito.service.security.SecurityImportService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImportExportService {
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;
    private final AccountRepository accountRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final SecurityTransactionRepository securityTransactionRepository;
    private final SecurityRepository securityRepository;
    private final SecurityImportService securityImportService;
    private final MoneyImportService moneyImportService;
    private final LoanRepository loanRepository;

    public ImportExportService(final MoneyTransactionGroupRepository moneyTransactionGroupRepository,
                               final AccountRepository accountRepository,
                               final MoneyTransactionRepository moneyTransactionRepository,
                               final SecurityTransactionRepository securityTransactionRepository,
                               final SecurityRepository securityRepository,
                               final SecurityImportService securityImportService,
                               final MoneyImportService moneyImportService,
                               final LoanRepository loanRepository) {
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
        this.accountRepository = accountRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.securityTransactionRepository = securityTransactionRepository;
        this.securityRepository = securityRepository;
        this.securityImportService = securityImportService;
        this.moneyImportService = moneyImportService;
        this.loanRepository = loanRepository;
    }

    public TikitoExportDto export(final long userId) {
        final TikitoExportDto export = new TikitoExportDto();

        final Map<Long, Account> accountsById = getAccountsById(userId);
        final Map<Long, Security> currenciesById = getCurrenciesById();

        exportMoneyGroups(userId, export, accountsById);
        exportAccounts(userId, export);
        exportLoans(userId, export, currenciesById);

        return export;
    }

    private void exportLoans(final long userId, final TikitoExportDto export, final Map<Long, Security> currenciesById) {
//        export.setLoans(loanRepository
//                .findByUserId(userId)
//                .stream()
//                .map(loan -> loan.toExportDto(currenciesById)));
    }

    private Map<Long, Account> getAccountsById(final long userId) {
        return accountRepository
                .findByUserId(userId)
                .stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
    }

    private Map<String, Account> getAccountsByName(final long userId) {
        return accountRepository
                .findByUserId(userId)
                .stream()
                .collect(Collectors.toMap(Account::getName, Function.identity()));
    }

    private Map<Long, Security> getCurrenciesById() {
        return securityRepository
                .findBySecurityType(SecurityType.CURRENCY)
                .stream()
                .collect(Collectors.toMap(Security::getId, Function.identity()));
    }

//    private Map<String, Security> getSecuritiesByIsin() {
//        final Map<String, Security> currenciesByIsin = new HashMap<>();
//        securityRepository
//                .findAll()
//                .forEach(security -> {
//                    security.getIsins().forEach(isin -> currenciesByIsin.put(isin.getIsin(), security));
//                });
//        return currenciesByIsin;
//    }

    public void exportAccounts(final long userId, final TikitoExportDto export) {
        export.setAccounts(accountRepository
                .findByUserId(userId)
                .stream()
                .map(this::exportAccount)
                .toList());
    }

    private AccountExportDto exportAccount(final Account account) {
        final AccountExportDto dto = account.toExportDto();

//        exportMoneyTransactions(dto, accountsById, currenciesById);
//        exportSecurityTransactions(dto, accountsById, currenciesById);

        return dto;
    }

    public void exportMoneyGroups(final long userId, final TikitoExportDto export, final Map<Long, Account> accountsById) {
        final List<MoneyTransactionGroup> groups = moneyTransactionGroupRepository.findByUserId(userId);

        export.setMoneyGroups(groups
                .stream()
                .map(group -> group.toExportDto(
                        group.getAccountIds()
                                .stream()
                                .map(accountId -> accountsById.get(accountId).getName())
                                .collect(Collectors.toSet())
                ))
                .toList());
    }

    public void exportMoneyTransactions(final long userId, final TikitoExportDto export, final Map<Long, Account> accountsById, final Map<Long, Security> currenciesById) {
//        export.setMoneyTransactions(moneyTransactionRepository
//                .findAllByUserId(userId)
//                .stream()
//                .map(transaction -> transaction.toExportDto(accountsById.get(transaction.getAccountId()).getName(), currenciesById.get(transaction.getCurrencyId()).getIsins().get(0).getIsin()))
//                .toList());
    }

    public void exportSecurityTransactions(final long userId, final TikitoExportDto export, final Map<Long, Account> accountsById, final Map<Long, Security> currenciesById) {
//        export.setSecurityTransactions(securityTransactionRepository
//                .findAllByUserId(userId)
//                .stream()
//                .map(transaction -> transaction.toExportDto(accountsById.get(transaction.getAccountId()).getName(), currenciesById.get(transaction.getCurrencyId()).getIsins().get(0).getIsin()))
//                .toList());
    }

    public void importFrom(final long userId, final TikitoExportDto dto) {
        importAccounts(userId, dto);

        final Map<String, Account> accountsByName = getAccountsByName(userId);
//        final Map<String, Security> securitiesByIsin = getSecuritiesByIsin();

        importMoneyTransactionGroups(userId, dto);
//        importMoneyTransactions(userId, dto, securitiesByIsin, accountsByName);
//        importSecurityTransactions(userId, dto, securitiesByIsin, accountsByName);

    }

    private void importMoneyTransactions(final long userId, final TikitoExportDto dto, final Map<String, Security> securitiesByIsin, final Map<String, Account> accountsByName) {
//        if (dto.getMoneyTransactions() == null) {
//            return;
//        }
//        log.info("Importing {} money transactions", dto.getMoneyTransactions().size());
//
//        moneyTransactionRepository.saveAllAndFlush(dto
//                .getMoneyTransactions()
//                .stream()
//                .map(transaction -> new MoneyTransaction(
//                        userId,
//                        accountsByName.get(transaction.getAccountName()).getId(),
//                        securitiesByIsin.get(transaction.getCurrency()).getId(),
//                        transaction))
//                .toList());
    }

    private void importSecurityTransactions(final long userId, final TikitoExportDto dto, final Map<String, Security> securitiesByIsin, final Map<String, Account> accountsByName) {
//        if (dto.getSecurityTransactions() == null) {
//            return;
//        }
//        log.info("Importing {} security transactions", dto.getSecurityTransactions().size());
//
//        final ImportSettings importSettings;
//        final SecurityTransactionImportResultDto result;
//        securityImportService.importTransactions(account, importSettings, result);
//        securityTransactionRepository.saveAllAndFlush(dto
//                .getSecurityTransactions()
//                .stream()
//                .map(transaction -> new SecurityTransaction(
//                        userId,
//                        accountsByName.get(transaction.getAccountName()).getId(),
//                        securitiesByIsin.get(transaction.getIsin()) == null ? null : securitiesByIsin.get(transaction.getIsin()).getId(),
//                        securitiesByIsin.get(transaction.getCurrency()).getId(),
//                        transaction))
//                .toList());
    }

    private void importAccounts(final long userId, final TikitoExportDto dto) {
        if (dto.getAccounts() == null) {
            return;
        }
        log.info("Importing {} accounts", dto.getAccounts().size());
        final Set<String> accountNames = accountRepository
                .findByUserId(userId)
                .stream()
                .map(Account::getName)
                .collect(Collectors.toSet());

        accountRepository.saveAllAndFlush(dto.getAccounts()
                .stream()
                .filter(account -> !accountNames.contains(account.getName()))
                .map(account -> new Account(userId, account))
                .toList());
    }

    public void importMoneyTransactionGroups(final long userId, final TikitoExportDto dto) {
        if (dto.getMoneyGroups() == null) {
            return;
        }
        log.info("Importing {} money transaction groups", dto.getMoneyGroups().size());
        final Set<String> moneyGroupNames = moneyTransactionGroupRepository
                .findByUserId(userId)
                .stream()
                .map(MoneyTransactionGroup::getName)
                .collect(Collectors.toSet());

        final List<MoneyTransactionGroup> groups = dto
                .getMoneyGroups()
                .stream()
                .filter(group -> !moneyGroupNames.contains(group.getName()))
                .filter(this::isValid)
                .map(group ->
                        new MoneyTransactionGroup(
                                userId,
                                accountRepository
                                        .findByUserIdAndName(userId, group.getAccountNames())
                                        .stream()
                                        .map(Account::getId)
                                        .collect(Collectors.toSet()),
                                group)
                ).toList();
        moneyTransactionGroupRepository.saveAllAndFlush(groups);
    }

    private boolean isValid(final MoneyTransactionGroupExportDto moneyTransactionGroupExportDto) {
        return true;
    }
}
