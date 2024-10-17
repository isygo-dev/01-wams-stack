package eu.isygoit.quartz.service;

import org.quartz.JobExecutionContext;

/**
 * The interface Job service.
 */
public interface JobService {

    /**
     * Process.
     *
     * @param context the context
     */
    void process(JobExecutionContext context);

    /**
     * Perform job.
     *
     * @param context the context
     */
    void performJob(JobExecutionContext context);
}
