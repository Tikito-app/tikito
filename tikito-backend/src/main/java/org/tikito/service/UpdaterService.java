package org.tikito.service;

import org.tikito.entity.Job;
import org.tikito.repository.MoneyTransactionRepository;
import org.tikito.repository.SecurityTransactionRepository;
import org.tikito.service.job.JobType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UpdaterService {
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final SecurityTransactionRepository securityTransactionRepository;
    private final JobService jobService;

    public UpdaterService(final MoneyTransactionRepository moneyTransactionRepository,
                          final SecurityTransactionRepository securityTransactionRepository,
                          final JobService jobService) {
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.securityTransactionRepository = securityTransactionRepository;
        this.jobService = jobService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void assertDataIsUpToDate() {
        updateCurrencies();
    }

    private void updateCurrencies() {
        final Set<Long> currencyIdsInUse = getCurrencyIdsInUse();
        currencyIdsInUse.forEach(currencyId -> jobService.addJob(Job.security(JobType.UPDATE_SECURITY_PRICES, currencyId).build()));

    }

    private Set<Long> getCurrencyIdsInUse() {
        final Set<Long> currencyIdsInUse = moneyTransactionRepository.getCurrencyIdsInUse();
        currencyIdsInUse.addAll(securityTransactionRepository.getCurrencyIdsInUse());
        return currencyIdsInUse;
    }
}
