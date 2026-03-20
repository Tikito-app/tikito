package org.tikito.service.money;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.controller.request.CreateOrUpdateMoneyTransactionRequest;
import org.tikito.dto.AccountType;
import org.tikito.dto.money.MoneyTransactionDto;
import org.tikito.dto.money.MoneyTransactionFilter;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.Job;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.repository.*;
import org.tikito.service.JobService;
import org.tikito.service.job.JobType;

import java.util.List;

@Slf4j
@Service
public class MoneyTransactionService {
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;
    private final AccountRepository accountRepository;
    private final SecurityRepository securityRepository;
    private final JobService jobService;
    private final BudgetRepository budgetRepository;
    private final LoanRepository loanRepository;

    public MoneyTransactionService(final MoneyTransactionRepository moneyTransactionRepository,
                                   final MoneyTransactionGroupRepository moneyTransactionGroupRepository,
                                   final AccountRepository accountRepository,
                                   final SecurityRepository securityRepository,
                                   final JobService jobService,
                                   final BudgetRepository budgetRepository,
                                   final LoanRepository loanRepository) {
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
        this.accountRepository = accountRepository;
        this.securityRepository = securityRepository;
        this.jobService = jobService;
        this.budgetRepository = budgetRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MoneyTransactionDto createOrUpdate(final long userId, final CreateOrUpdateMoneyTransactionRequest request) {
        final MoneyTransaction transaction = request.isNew() ? new MoneyTransaction(userId) : moneyTransactionRepository.findByUserIdAndId(userId, request.getId()).orElseThrow();
        accountRepository.findByUserIdAndIdAndAccountType(userId, request.getAccountId(), AccountType.DEBIT).orElseThrow();
        securityRepository.findByIdAndSecurityType(request.getCurrencyId(), SecurityType.CURRENCY).orElseThrow();
        if(request.getGroupId() != null) {
            moneyTransactionGroupRepository.findByUserIdAndId(userId, request.getGroupId()).orElseThrow();
        }
        if(request.getBudgetId() != null) {
            budgetRepository.findByUserIdAndId(userId, request.getBudgetId()).orElseThrow();
        }
        if(request.getLoanId() != null) {
            loanRepository.findByUserIdAndId(userId, request.getLoanId()).orElseThrow();
        }
        transaction.setAccountId(request.getAccountId());
        transaction.setCounterpartAccountName(request.getCounterpartAccountName());
        transaction.setCounterpartAccountNumber(request.getCounterpartAccountNumber());
        transaction.setTimestamp(request.getTimestamp());
        transaction.setAmount(request.getAmount());
        transaction.setFinalBalance(request.getFinalBalance());
        transaction.setDescription(request.getDescription());
        transaction.setCurrencyId(request.getCurrencyId());
        transaction.setGroupId(request.getGroupId());
        transaction.setBudgetId(request.getBudgetId());
        transaction.setLoanId(request.getLoanId());
        transaction.setExchangeRate(request.getExchangeRate());

        return moneyTransactionRepository.saveAndFlush(transaction).toDto();
    }

    public List<MoneyTransactionDto> getTransactions(final long userId, final MoneyTransactionFilter filter) {
        return moneyTransactionRepository
                .findByFilter(
                        userId,
                        filter.getAccountIds(),
                        filter.getGroupIds(),
                        filter.getNonGrouped() != null && filter.getNonGrouped(),
                        filter.getStartDateAsInstant(),
                        filter.getEndDateAsInstant())
                .stream()
                .map(MoneyTransaction::toDto)
                .toList();
    }

    public List<MoneyTransactionDto> getTransactionsForLoans(final long userId) {
        return moneyTransactionRepository
                .findByUserIdAndLoanIdNotNull(userId)
                .stream()
                .map(MoneyTransaction::toDto)
                .toList();

    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MoneyTransaction setTransactionGroupId(final long userId, final long transactionId, final Long groupId) {
        final MoneyTransaction transaction = moneyTransactionRepository.findByUserIdAndId(userId, transactionId).orElseThrow();
        if (groupId != null) {
            moneyTransactionGroupRepository.findById(groupId).orElseThrow();
        }
        transaction.setGroupId(groupId);
        return moneyTransactionRepository.saveAndFlush(transaction);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteTransaction(final long userId, final long transactionId) {
        final MoneyTransaction transaction = moneyTransactionRepository.findByUserIdAndId(userId, transactionId).orElseThrow();
        moneyTransactionRepository.deleteByUserIdAndId(userId, transactionId);
        jobService.addJob(Job.account(JobType.RECALCULATE_HISTORICAL_MONEY_VALUES, transaction.getAccountId(), userId).build());
        jobService.addJob(Job.account(JobType.RECALCULATE_AGGREGATED_HISTORICAL_MONEY_VALUES, userId).build());
    }
}
