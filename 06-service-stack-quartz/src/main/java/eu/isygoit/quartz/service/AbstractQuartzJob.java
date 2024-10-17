package eu.isygoit.quartz.service;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * The type Abstract quartz job.
 */
public abstract class AbstractQuartzJob extends QuartzJobBean implements QuartzJob {

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (this.getJobService() != null) {
            this.getJobService().process(jobExecutionContext);
        }
    }
}
