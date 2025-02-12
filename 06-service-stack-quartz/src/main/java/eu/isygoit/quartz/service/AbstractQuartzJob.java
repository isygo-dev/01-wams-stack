package eu.isygoit.quartz.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Optional;

/**
 * Abstract base class for Quartz jobs providing default behavior for job execution.
 * <p>
 * This class ensures that the job service is available before processing the job.
 * It also implements a logging mechanism to track job execution.
 * </p>
 */
@Slf4j // Lombok annotation for SLF4J logging
public abstract class AbstractQuartzJob extends QuartzJobBean implements QuartzJob {

    /**
     * Executes the Quartz job by processing the job context using the associated job service.
     *
     * @param jobExecutionContext the context of the job to execute.
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        // Log the start of the job execution
        log.info("Starting job execution for job: {}", jobExecutionContext.getJobDetail().getKey());

        // Optional check for job service and process the job if available
        Optional.ofNullable(this.getJobService())
                .ifPresentOrElse(
                        jobService -> {
                            try {
                                jobService.process(jobExecutionContext); // Process the job
                                log.info("Job executed successfully for job: {}", jobExecutionContext.getJobDetail().getKey());
                            } catch (Exception e) {
                                // Log the error and rethrow it as JobExecutionException
                                log.error("Error processing job: {}", jobExecutionContext.getJobDetail().getKey(), e);
                                throw new RuntimeException(e);
                            }
                        },
                        () -> {
                            log.warn("Job service is not available for job: {}", jobExecutionContext.getJobDetail().getKey());
                            throw new RuntimeException("jobService is null in Job " + this.getClass().getSimpleName());
                            // Optionally throw an exception if the job service is essential
                        }
                );
    }
}