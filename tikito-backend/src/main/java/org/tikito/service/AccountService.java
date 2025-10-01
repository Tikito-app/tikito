package org.tikito.service;

import org.tikito.controller.request.CreateOrUpdateAccountRequest;
import org.tikito.dto.AccountDto;
import org.tikito.dto.AccountType;
import org.tikito.entity.Account;
import org.tikito.entity.Job;
import org.tikito.exception.AccountNumberAlreadyExistsException;
import org.tikito.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.repository.*;
import org.tikito.service.job.JobType;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final SecurityRepository securityRepository;
    private final SecurityTransactionRepository securityTransactionRepository;
    private final SecurityHoldingRepository securityHoldingRepository;
    private final HistoricalSecurityHoldingValueRepository historicalSecurityHoldingValueRepository;
    private final AggregatedHistoricalSecurityHoldingValueRepository aggregatedHistoricalSecurityHoldingValueRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final HistoricalMoneyHoldingValueRepository historicalMoneyHoldingValueRepository;
    private final AggregatedHistoricalMoneyHoldingValueRepository aggregatedHistoricalMoneyHoldingValueRepository;
    private final JobService jobService;

    public AccountService(final AccountRepository accountRepository,
                          final SecurityRepository securityRepository,
                          final SecurityTransactionRepository securityTransactionRepository,
                          final SecurityHoldingRepository securityHoldingRepository,
                          final HistoricalSecurityHoldingValueRepository historicalSecurityHoldingValueRepository,
                          final AggregatedHistoricalSecurityHoldingValueRepository aggregatedHistoricalSecurityHoldingValueRepository,
                          final MoneyTransactionRepository moneyTransactionRepository,
                          final HistoricalMoneyHoldingValueRepository historicalMoneyHoldingValueRepository,
                          final AggregatedHistoricalMoneyHoldingValueRepository aggregatedHistoricalMoneyHoldingValueRepository,
                          final JobService jobService) {
        this.accountRepository = accountRepository;
        this.securityRepository = securityRepository;
        this.securityTransactionRepository = securityTransactionRepository;
        this.securityHoldingRepository = securityHoldingRepository;
        this.historicalSecurityHoldingValueRepository = historicalSecurityHoldingValueRepository;
        this.aggregatedHistoricalSecurityHoldingValueRepository = aggregatedHistoricalSecurityHoldingValueRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.historicalMoneyHoldingValueRepository = historicalMoneyHoldingValueRepository;
        this.aggregatedHistoricalMoneyHoldingValueRepository = aggregatedHistoricalMoneyHoldingValueRepository;
        this.jobService = jobService;
    }

    public AccountDto getAccount(final long userId, final long accountId) {
        return accountRepository
                .findByUserIdAndId(userId, accountId)
                .map(Account::toDto)
                .orElseThrow();
    }

    public List<AccountDto> getAccounts(final long userId) {
        return accountRepository
                .findByUserId(userId)
                .stream()
                .map(Account::toDto)
                .toList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public AccountDto createOrUpdate(final long userId, final CreateOrUpdateAccountRequest request) throws ResourceNotFoundException {
        Account account = new Account(userId);
        if (!securityRepository.existsById(request.getCurrencyId())) {
            throw new ResourceNotFoundException(request.getCurrencyId());
        }
        if (request.getId() != 0) {
            account = accountRepository.findByUserIdAndId(userId, request.getId()).orElseThrow();
        }
        if(accountRepository.countByAccountNumberAndNotMyId(userId, request.getAccountNumber(), account.getId()) > 0) {
            throw new AccountNumberAlreadyExistsException();
        }
        account.setName(request.getName());
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setCurrencyId(request.getCurrencyId());

        return accountRepository
                .saveAndFlush(account)
                .toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteAccount(final long userId, final long accountId) {
        final Optional<Account> maybeAccount = accountRepository.findByUserIdAndId(userId, accountId);
        maybeAccount.ifPresent((account -> {
            if (account.getAccountType() == AccountType.SECURITY) {
                deleteSecuritiesUnderAccount(userId, account.getId());
            } else {
                deleteMoneyHoldingUnderAccount(userId, account.getId());
            }
            accountRepository.deleteByUserIdAndId(userId, accountId);
        }));
    }

    private void deleteMoneyHoldingUnderAccount(final long userId, final long accountId) {
        moneyTransactionRepository.deleteByAccountId(accountId);
        historicalMoneyHoldingValueRepository.deleteByAccountId(accountId);
        aggregatedHistoricalMoneyHoldingValueRepository.deleteByAccountIds(accountId);
        jobService.addJob(Job.account(JobType.RECALCULATE_AGGREGATED_HISTORICAL_MONEY_VALUES, userId).build());
    }

    private void deleteSecuritiesUnderAccount(final long userId, final long accountId) {
        securityTransactionRepository.deleteByAccountId(accountId);
        securityHoldingRepository.deleteByAccountIds(accountId);
        historicalSecurityHoldingValueRepository.deleteByAccountIds(accountId);
        aggregatedHistoricalSecurityHoldingValueRepository.deleteByAccountIds(accountId);
        jobService.addJob(Job.account(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userId).build());
    }
}
