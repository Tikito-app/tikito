package org.tikito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.entity.Job;
import org.tikito.repository.JobRepository;
import org.tikito.repository.SecurityTransactionRepository;
import org.tikito.service.job.JobExecutor;
import org.tikito.service.job.JobType;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final JobExecutor jobExecutor;
    private final SecurityTransactionRepository securityTransactionRepository;

    public JobService(final JobRepository jobRepository,
                      final JobExecutor jobExecutor,
                      final SecurityTransactionRepository securityTransactionRepository) {
        this.jobRepository = jobRepository;
        this.jobExecutor = jobExecutor;
        this.securityTransactionRepository = securityTransactionRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void addJob(final Job job) {
        switch (job.getJobType()) {
            case RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES:
            case RECALCULATE_AGGREGATED_HISTORICAL_MONEY_VALUES:
            case GROUP_MONEY_TRANSACTIONS:
                // delete the previous job, because it is not up to date anymore
                jobRepository.deleteByJobTypeAndUserId(job.getJobType(), job.getUserId());
                break;
            case RECALCULATE_HISTORICAL_SECURITY_VALUES:
                jobRepository.deleteByJobTypeAndUserIdAndSecurityId(job.getJobType(), job.getUserId(), job.getSecurityId());
                break;
            case RECALCULATE_HISTORICAL_MONEY_VALUES:
                jobRepository.deleteByJobTypeAndUserIdAndAccountId(job.getJobType(), job.getUserId(), job.getAccountId());
                break;
        }

        jobRepository.saveAndFlush(job);
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
        securityTransactionRepository.findSecurityIdsByUserId(userId)
                .forEach(securityId -> {
                    addJob(Job.security(JobType.ENRICH_SECURITY, securityId).build());
                    addJob(Job.security(JobType.UPDATE_SECURITY_PRICES, securityId).build());
                    addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, securityId, userId).build());
                });
        addJob(Job.account(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userId).build());
    }

    @Transactional
    public void updateAllSecurityValues(final long userId) {
        securityTransactionRepository.findSecurityIdsByUserId(userId)
                .forEach(securityId -> {
                    addJob(Job.security(JobType.RECALCULATE_HISTORICAL_SECURITY_VALUES, securityId, userId).build());
                });
        addJob(Job.account(JobType.RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userId).build());
    }
}
