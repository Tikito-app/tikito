package org.tikito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.entity.Job;
import org.tikito.repository.JobRepository;
import org.tikito.repository.SecurityHoldingRepository;
import org.tikito.repository.SecurityTransactionRepository;
import org.tikito.service.job.JobExecutor;
import org.tikito.service.job.JobType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final JobExecutor jobExecutor;
    private final SecurityTransactionRepository securityTransactionRepository;
    private final JobFactoryService jobFactoryService;
    private final SecurityHoldingRepository securityHoldingRepository;

    public JobService(final JobRepository jobRepository,
                      final JobExecutor jobExecutor,
                      final JobFactoryService jobFactoryService,
                      final SecurityTransactionRepository securityTransactionRepository,
                      final SecurityHoldingRepository securityHoldingRepository) {
        this.jobRepository = jobRepository;
        this.jobExecutor = jobExecutor;
        this.securityTransactionRepository = securityTransactionRepository;
        this.securityHoldingRepository = securityHoldingRepository;
        this.jobFactoryService = jobFactoryService;
    }

    @Scheduled(fixedDelay = 5000L)
    public void processAllJobs() {
        final List<Job> processedJobs = new ArrayList<>();

        jobRepository.findAllOrdered().forEach(job -> {
            try {
                jobExecutor.process(job);
            } catch (final Exception e) {
                log.error("Error executing job {}", job.getId(), e);
            }
            processedJobs.add(job);
        });
        jobRepository.deleteAll(processedJobs);
    }

    @Transactional
    public void updateAllSecurities(final long userId) {
        final Set<Long> currencyIdsProcessed = new HashSet<>();
        securityTransactionRepository.findAllByUserId(userId)
                .forEach(transaction -> {
                    final Long securityId = transaction.getId();

                    // Be sure to also update the currency prices, before recalculating the historical values
                    jobFactoryService.addJobToUpdateCurrencyPrice(transaction, currencyIdsProcessed, securityId);
                    jobFactoryService.addJob(Job.security(JobType.ENRICH_SECURITY, securityId).build());
                    jobFactoryService.addJob(Job.security(JobType.UPDATE_SECURITY_PRICES, securityId).build());
                    jobFactoryService.addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, transaction.getSecurityId(), userId).build());
                });
        jobFactoryService.addJob(Job.user(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userId).build());
    }

    @Transactional
    public void updateAllSecurityValues(final long userId) {
        securityHoldingRepository.findByUserId(userId).forEach(holding ->
                jobFactoryService.addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, holding.getSecurityId(), userId).build()));

        jobFactoryService.addJob(Job.user(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userId).build());
    }

    @Transactional(readOnly = true)
    public long getJobsCount(final long userId) {
        return jobRepository.countByUserId(userId);
    }
}
