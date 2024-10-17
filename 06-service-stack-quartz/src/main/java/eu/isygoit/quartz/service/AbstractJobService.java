package eu.isygoit.quartz.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Abstract job service.
 */
@Slf4j
public abstract class AbstractJobService implements JobService {

    @Override
    @Transactional
    public void process(JobExecutionContext context) {
        log.info("START EXECUTING JOB: {}", this.getClass().getSimpleName());
        try {
            this.performJob(context);
        } catch (Exception e) {
            log.error("<ERROR>:  ", e);
        }
        log.info("COMPLETE EXECUTING JOB: {}", this.getClass().getSimpleName());
    }
}
