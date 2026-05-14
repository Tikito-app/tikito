package org.tikito.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.entity.Job;
import org.tikito.repository.*;
import org.tikito.service.job.JobType;

import java.util.Set;

import static org.tikito.service.job.JobType.*;

@Service
public class UpdaterService {
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final SecurityTransactionRepository securityTransactionRepository;
    private final SecurityHoldingRepository securityHoldingRepository;
    private final UserAccountRepository userAccountRepository;
    private final MoneyHoldingRepository moneyHoldingRepository;
    private final JobFactoryService jobFactoryService;

    public UpdaterService(final MoneyTransactionRepository moneyTransactionRepository,
                          final SecurityTransactionRepository securityTransactionRepository,
                          final SecurityHoldingRepository securityHoldingRepository,
                          final UserAccountRepository userAccountRepository,
                          final MoneyHoldingRepository moneyHoldingRepository,
                          final JobFactoryService jobFactoryService) {
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.securityTransactionRepository = securityTransactionRepository;
        this.securityHoldingRepository = securityHoldingRepository;
        this.userAccountRepository = userAccountRepository;
        this.moneyHoldingRepository = moneyHoldingRepository;
        this.jobFactoryService = jobFactoryService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void updateSystemData() {
        updateCurrencies();
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateUserData() {
        updateSecurities();
        updateMoney();
    }

    private void updateCurrencies() {
        final Set<Long> currencyIdsInUse = getCurrencyIdsInUse();
        currencyIdsInUse.forEach(currencyId ->
                jobFactoryService.addJob(Job.security(JobType.UPDATE_SECURITY_PRICES, currencyId).build()));
    }

    private void updateSecurities() {
        securityHoldingRepository.findAll().forEach(securityHolding ->
                jobFactoryService.addJob(Job.security(RECALCULATE_HISTORICAL_SECURITY_VALUES, securityHolding.getSecurityId(), securityHolding.getUserId()).build()));

        userAccountRepository.findAll().forEach(userAccount ->
                jobFactoryService.addJob(Job.account(RECALCULATE_AGGREGATED_HISTORICAL_SECURITY_VALUES, userAccount.getId()).build()));
    }

    private void updateMoney() {
        moneyHoldingRepository.findAll().forEach(moneyHolding ->
                jobFactoryService.addJob(Job.account(RECALCULATE_HISTORICAL_MONEY_VALUES, moneyHolding.getAccountId(), moneyHolding.getUserId()).build()));

        userAccountRepository.findAll().forEach(userAccount ->
                jobFactoryService.addJob(Job.account(RECALCULATE_AGGREGATED_HISTORICAL_MONEY_VALUES, userAccount.getId()).build()));
    }

    private Set<Long> getCurrencyIdsInUse() {
        final Set<Long> currencyIdsInUse = moneyTransactionRepository.getCurrencyIdsInUse();
        currencyIdsInUse.addAll(securityTransactionRepository.getCurrencyIdsInUse());
        return currencyIdsInUse;
    }
}
