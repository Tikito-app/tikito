package org.tikito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.entity.Job;
import org.tikito.entity.security.SecurityTransaction;
import org.tikito.repository.JobRepository;
import org.tikito.service.job.JobType;

import java.util.Set;

@Service
@Slf4j
public class JobFactoryService {
    private final JobRepository jobRepository;

    public JobFactoryService(final JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void addJob(final Job job) {
        switch (job.getJobType()) {
            case RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES:
            case RECALCULATE_AGGREGATED_HISTORICAL_MONEY_VALUES:
            case GROUP_MONEY_TRANSACTIONS:
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

    @Transactional
    public void addJobToUpdateCurrencyPrice(final SecurityTransaction security, final Set<Long> currencyIdsProcessed, final Long securityId) {
        if (!currencyIdsProcessed.contains(security.getCurrencyId())) {
            addJob(Job.security(JobType.UPDATE_SECURITY_PRICES, securityId).build());
            currencyIdsProcessed.add(security.getCurrencyId());
        }
    }
}
