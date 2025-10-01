package org.tikito.service.money;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.money.MoneyTransactionDto;
import org.tikito.dto.money.MoneyTransactionFilter;
import org.tikito.entity.Job;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.repository.MoneyTransactionGroupRepository;
import org.tikito.repository.MoneyTransactionRepository;
import org.tikito.service.JobService;
import org.tikito.service.job.JobType;

import java.util.List;

@Slf4j
@Service
public class MoneyTransactionService {
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final MoneyTransactionGroupRepository moneyTransactionGroupRepository;
    private final JobService jobService;

    public MoneyTransactionService(final MoneyTransactionRepository moneyTransactionRepository,
                                   final MoneyTransactionGroupRepository moneyTransactionGroupRepository,
                                   final JobService jobService) {
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.moneyTransactionGroupRepository = moneyTransactionGroupRepository;
        this.jobService = jobService;
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
