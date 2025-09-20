package org.tikito.service.job;

import org.tikito.entity.Job;
import org.tikito.exception.InvalidJobException;
import org.tikito.service.money.MoneyHoldingService;
import org.tikito.service.money.MoneyTransactionGroupService;
import org.tikito.service.security.SecurityHoldingService;
import org.tikito.service.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobExecutor {
    private final List<JobProcessor> processors;

    public JobExecutor(final SecurityService securityService,
                       final SecurityHoldingService securityHoldingService,
                       final MoneyHoldingService moneyHoldingService,
                       final MoneyTransactionGroupService moneyTransactionGroupService) {
        processors = new ArrayList<>();
        processors.add(securityService);
        processors.add(securityHoldingService);
        processors.add(moneyHoldingService);
        processors.add(moneyTransactionGroupService);

    }

    @Transactional
    public void process(final Job job) throws InvalidJobException {
        for (final JobProcessor processor : processors) {
            if (processor.canProcess(job)) {
                processor.process(job);
                return;
            }
        }

        throw new InvalidJobException();
    }
}
