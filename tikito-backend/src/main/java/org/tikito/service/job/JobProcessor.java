package org.tikito.service.job;

import org.tikito.entity.Job;

public interface JobProcessor {
    boolean canProcess(Job job);

    void process(Job job);
}
