package org.tikito.service.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.security.SecurityHoldingFilter;
import org.tikito.dto.security.SecurityTransactionDto;
import org.tikito.entity.Job;
import org.tikito.entity.security.SecurityHolding;
import org.tikito.entity.security.SecurityTransaction;
import org.tikito.repository.*;
import org.tikito.service.CacheService;
import org.tikito.service.JobService;
import org.tikito.service.importer.security.SecurityTransactionImporter;
import org.tikito.service.job.JobType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SecurityTransactionService {

    private final SecurityTransactionRepository securityTransactionRepository;
    private final SecurityHoldingRepository securityHoldingRepository;
    private final JobService jobService;
    private final CacheService cacheService;

    public SecurityTransactionService(final SecurityTransactionRepository securityTransactionRepository, final SecurityHoldingRepository securityHoldingRepository, final JobService jobService, final CacheService cacheService) {
        this.securityTransactionRepository = securityTransactionRepository;
        this.securityHoldingRepository = securityHoldingRepository;
        this.jobService = jobService;
        this.cacheService = cacheService;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteTransaction(final long userId, final long transactionId) {
        final SecurityTransaction transaction = securityTransactionRepository.findByUserIdAndId(userId, transactionId).orElseThrow();
        securityTransactionRepository.deleteByUserIdAndId(userId, transactionId);
        jobService.addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, transaction.getSecurityId(), userId).build());
        jobService.addJob(Job.account(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userId).build());
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
}
