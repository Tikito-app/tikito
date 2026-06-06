package org.tikito.service.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.controller.request.CreateOrUpdateSecurityTransactionRequest;
import org.tikito.dto.security.SecurityHoldingFilter;
import org.tikito.dto.security.SecurityTransactionDto;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.Job;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.SecurityTransaction;
import org.tikito.repository.AccountRepository;
import org.tikito.repository.IsinRepository;
import org.tikito.repository.SecurityRepository;
import org.tikito.repository.SecurityTransactionRepository;
import org.tikito.service.CacheService;
import org.tikito.service.JobFactoryService;
import org.tikito.service.job.JobType;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class SecurityTransactionService {

    private final SecurityTransactionRepository securityTransactionRepository;
    private final JobFactoryService jobFactoryService;
    private final AccountRepository accountRepository;
    private final IsinRepository isinRepository;
    private final SecurityRepository securityRepository;

    public SecurityTransactionService(final SecurityTransactionRepository securityTransactionRepository,
                                      final JobFactoryService jobFactoryService,
                                      final AccountRepository accountRepository,
                                      final IsinRepository isinRepository,
                                      final SecurityRepository securityRepository) {
        this.securityTransactionRepository = securityTransactionRepository;
        this.jobFactoryService = jobFactoryService;
        this.accountRepository = accountRepository;
        this.isinRepository = isinRepository;
        this.securityRepository = securityRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SecurityTransactionDto createOrUpdate(final long userId, final CreateOrUpdateSecurityTransactionRequest request) {
        final Isin isin = isinRepository.findById(request.getIsin()).orElseThrow();
        final SecurityTransaction transaction = request.isNew() ? new SecurityTransaction(userId) : securityTransactionRepository.findByUserIdAndId(userId, request.getId()).orElseThrow();

        accountRepository.findByUserIdAndId(userId, request.getAccountId()).orElseThrow();
        securityRepository.findByIdAndSecurityTypes(request.getCurrencyId(), Set.of(SecurityType.CURRENCY, SecurityType.CRYPTO)).orElseThrow();

        transaction.updateFrom(request, isin.getSecurityId());
        return securityTransactionRepository.saveAndFlush(transaction).toDto();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteTransaction(final long userId, final long transactionId) {
        final SecurityTransaction transaction = securityTransactionRepository.findByUserIdAndId(userId, transactionId).orElseThrow();
        securityTransactionRepository.deleteByUserIdAndId(userId, transactionId);
        jobFactoryService.addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, transaction.getSecurityId(), userId).build());
        jobFactoryService.addJob(Job.user(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userId).build());
    }

    public List<SecurityTransactionDto> getSecurityTransactions(final long userId, final SecurityHoldingFilter filter) {
        return enrichTransactions(securityTransactionRepository
                .findBySecurityIdIn(userId, filter.getSecurityIds(), filter.getAccountIds(), filter.getStartDateAsInstant())
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
                    if (transaction.getSecurityId() != null) {
                        transaction.setSecurity(CacheService.getSecurity(transaction.getSecurityId()));
                    }
                    return transaction;
                })
                .toList();
    }
}
