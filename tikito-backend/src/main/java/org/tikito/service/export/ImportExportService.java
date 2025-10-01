package org.tikito.service.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tikito.dto.export.*;
import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.dto.security.SecurityTransactionImportResultDto;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.Account;
import org.tikito.entity.loan.Loan;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.security.Security;
import org.tikito.repository.*;
import org.tikito.service.money.MoneyImportService;
import org.tikito.service.security.SecurityImportService;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public TikitoExportDto export(final long userId, final ImportExportSettings settings) {
        final TikitoExportDto export = new TikitoExportDto();

        final Map<Long, Account> accountsById = getAccountsById(userId);
        final Map<String, Account> accountsByName = getAccountsByName(userId);
        final Map<Long, Security> currenciesById = getCurrenciesById();

        if(settings.isAccounts()) {
            exportAccounts(userId, export, currenciesById);
        }

        export.getAccounts().forEach(account -> {
            if(settings.isSecurityTransactions()) {
                exportSecurityTransactions(accountsByName.get(account.getName()), account, currenciesById);
            }
            if(settings.isMoneyTransactions()) {
                exportMoneyTransactions(accountsByName.get(account.getName()), account, currenciesById);
            }
        });
        if(settings.isMoneyTransactionGroups()) {
            exportMoneyGroups(userId, export, accountsById);
        }
        if(settings.isLoans()) {
            exportLoans(userId, export, currenciesById);
        }

        return export;
    }

    private void exportLoans(final long userId, final TikitoExportDto export, final Map<Long, Security> currenciesById) {
        export.setLoans(loanRepository
                .findByUserId(userId)
                .stream()
                .map(loan -> loan.toExportDto(currenciesById))
                .toList());
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

    private Map<String, MoneyTransactionGroup> getMoneyTransactionGroupsByName(final long userId) {
        return moneyTransactionGroupRepository
                .findByUserId(userId)
                .stream()
                .collect(Collectors.toMap(MoneyTransactionGroup::getName, Function.identity()));
    }

    private Map<Long, Security> getCurrenciesById() {
        return securityRepository
                .findBySecurityType(SecurityType.CURRENCY)
                .stream()
                .collect(Collectors.toMap(Security::getId, Function.identity()));
    }

    private Map<String, Security> getCurrenciesByIsin() {
        return securityRepository
                .findBySecurityType(SecurityType.CURRENCY)
                .stream()
                .collect(Collectors.toMap(Security::getCurrentIsin, Function.identity()));
    }

    public void exportAccounts(final long userId, final TikitoExportDto export, final Map<Long, Security> currenciesById) {
        export.setAccounts(accountRepository
                .findByUserId(userId)
                .stream()
                .map(account -> account.toExportDto(currenciesById))
                .toList());
    }

    public void exportMoneyGroups(final long userId, final TikitoExportDto export, final Map<Long, Account> accountsById) {
        final List<MoneyTransactionGroup> groups = moneyTransactionGroupRepository.findByUserId(userId);

        export.setMoneyGroups(groups
                .stream()
                .map(group -> group.toExportDto(accountsById))
                .toList());
    }

    public void exportMoneyTransactions(final Account account, final AccountExportDto accountExportDto, final Map<Long, Security> currenciesById) {
        accountExportDto.setMoneyTransactions(moneyTransactionRepository
                .findByAccountId(account.getId())
                .stream()
                .map(transaction -> transaction.toExportDto(accountExportDto.getName(), currenciesById.get(transaction.getCurrencyId()).getCurrentIsin())) // todo: replace with proper isin
                .toList());
    }

    public void exportSecurityTransactions(final Account account, final AccountExportDto accountExportDto, final Map<Long, Security> currenciesById) {
        accountExportDto.setSecurityTransactions(securityTransactionRepository
                .findByAccountId(account.getId())
                .stream()
                .map(transaction -> transaction.toExportDto(accountExportDto.getName(), currenciesById.get(transaction.getCurrencyId()).getCurrentIsin())) // todo: replace with proper isin
                .toList());
    }

    public void importFrom(final long userId, final TikitoExportDto dto, final ImportExportSettings settings) {
        final Map<String, Security> currenciesByIsin = getCurrenciesByIsin();
        if(settings.isAccounts()) {
            importAccounts(userId, dto, currenciesByIsin);
        }

        final Map<String, Account> accountsByName = getAccountsByName(userId);
        if(settings.isMoneyTransactionGroups()) {
            importMoneyTransactionGroups(userId, dto);
        }

        final Map<String, MoneyTransactionGroup> moneyTransactionGroupsByName = getMoneyTransactionGroupsByName(userId);
        // todo add group information to imported transactions

        if(settings.isMoneyTransactions()) {
            importMoneyTransactions(dto, accountsByName);
        }
        if(settings.isSecurityTransactions()) {
            importSecurityTransactions(dto, accountsByName);
        }
        if(settings.isLoans()) {
            importLoans(userId, dto, moneyTransactionGroupsByName, currenciesByIsin);
        }
    }

    private void importMoneyTransactions(final TikitoExportDto dto, final Map<String, Account> accountsByName) {
        dto.getAccounts().forEach(importedAccount -> {
            final Account account = accountsByName.get(importedAccount.getName());
            if (account == null) {
                log.warn("Cannot find account {}", importedAccount.getName());
            } else {
                log.info("Importing {} money transactions to account {}", importedAccount.getMoneyTransactions().size(), importedAccount.getName());
                final MoneyTransactionImportResultDto resultDto = new MoneyTransactionImportResultDto(
                        importedAccount
                                .getMoneyTransactions()
                                .stream()
                                .map(MoneyTransactionExportDto::toImportLine)
                                .toList());
                moneyImportService.importTransactions(account.toDto(), resultDto, false);
            }
        });
    }

    private void importSecurityTransactions(final TikitoExportDto dto, final Map<String, Account> accountsByName) {
        dto.getAccounts().forEach(importedAccount -> {
            final Account account = accountsByName.get(importedAccount.getName());
            if (account == null) {
                log.warn("Cannot find account {}", importedAccount.getName());
            } else {
                log.info("Importing {} security transactions to account {}", importedAccount.getSecurityTransactions().size(), importedAccount.getName());
                final SecurityTransactionImportResultDto resultDto = new SecurityTransactionImportResultDto(List.of());
                resultDto.setImportedTransactions(
                        importedAccount
                                .getSecurityTransactions()
                                .stream()
                                .map(SecurityTransactionExportDto::toImportLine)
                                .toList());
                securityImportService.importTransactions(account.toDto(), resultDto, true);
            }
        });
    }

    private void importAccounts(final long userId, final TikitoExportDto dto, final Map<String, Security> currenciesByIsin) {
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
                .map(account -> new Account(userId, account, currenciesByIsin))
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

    private void importLoans(final long userId, final TikitoExportDto dto, final Map<String, MoneyTransactionGroup> moneyTransactionGroupsByName, final Map<String, Security> currenciesByIsin) {
        if(dto.getLoans() == null) {
            return ;
        }
        log.info("Importing {} loans", dto.getLoans().size());
        loanRepository.saveAllAndFlush(dto.getLoans()
                .stream()
                .map(loan -> new Loan(userId, loan, moneyTransactionGroupsByName, currenciesByIsin))
                .toList());
    }

    private boolean isValid(final MoneyTransactionGroupExportDto moneyTransactionGroupExportDto) {
        return true;
    }
}
