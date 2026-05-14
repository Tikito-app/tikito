package org.tikito.service.job;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.entity.Job;
import org.tikito.exception.InvalidJobException;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobExecutor {
    private final List<JobProcessor> processors = new ArrayList<>();

    public JobExecutor(final List<JobProcessor> processors) {
        this.processors.addAll(processors);
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
