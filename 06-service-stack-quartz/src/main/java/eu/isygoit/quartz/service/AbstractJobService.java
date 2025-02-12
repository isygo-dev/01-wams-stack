package eu.isygoit.quartz.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Abstract base class for job services that provides common functionality for processing jobs.
 * It handles job execution lifecycle, including logging and exception handling.
 */
@Slf4j
public abstract class AbstractJobService implements JobService {

    /**
     * Processes the job execution by invoking the specific job logic and handling transaction.
     * Logs the start and completion of the job execution.
     *
     * @param context the job execution context
     */
    @Override
    @Transactional
    public void process(JobExecutionContext context) {
        var jobName = this.getClass().getSimpleName();
        log.info("Job execution initiated: [{}] started at {}", jobName, System.currentTimeMillis());

        // Try executing the job and handle exceptions
        Optional.ofNullable(context)
                .ifPresentOrElse(
                        ctx -> performWithLogging(ctx, jobName), // Perform the job with logging
                        () -> log.error("Job execution failed: [{}] - JobExecutionContext is null. Aborting execution.", jobName) // Log error if context is null
                );

        log.info("Job execution completed: [{}] finished at {}", jobName, System.currentTimeMillis());
    }

    /**
     * Executes the job with enhanced logging and error handling.
     *
     * @param context the job execution context
     * @param jobName the name of the job
     */
    private void performWithLogging(JobExecutionContext context, String jobName) {
        try {
            log.debug("Performing job [{}] with execution context: {}", jobName, context);
            performJob(context); // Perform the actual job
            log.info("Job [{}] completed successfully.", jobName); // Success log
        } catch (Exception e) {
            log.error("Job execution failed: [{}] encountered an error. Exception details:", jobName, e); // Detailed error log
        }
    }
}