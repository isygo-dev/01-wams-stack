package eu.isygoit.quartz.service;

import eu.isygoit.quartz.types.SingleJobData;
import org.quartz.*;

import java.time.Duration;
import java.util.Date;


/**
 * The interface Quartz service.
 */
public interface QuartzService {

    /**
     * Create cron schedule builder cron schedule builder.
     *
     * @param jobDetail      the job detail
     * @param identity       the identity
     * @param group          the group
     * @param cronExpression the cron expression
     * @return the cron schedule builder
     */
    CronScheduleBuilder createCronScheduleBuilder(JobDetail jobDetail, String identity, String group, CronExpression cronExpression);

    /**
     * Create cron schedule builder cron schedule builder.
     *
     * @param jobDetail the job detail
     * @param identity  the identity
     * @param group     the group
     * @param cron      the cron
     * @return the cron schedule builder
     */
    CronScheduleBuilder createCronScheduleBuilder(JobDetail jobDetail, String identity, String group, String cron);

    /**
     * Create job detail job detail.
     *
     * @param jobClass   the job class
     * @param jobDataMap the job data map
     * @return the job detail
     */
    JobDetail createJobDetail(Class<? extends Job> jobClass, JobDataMap jobDataMap);

    /**
     * Create job detail job detail.
     *
     * @param jobClass   the job class
     * @param identity   the identity
     * @param group      the group
     * @param jobDataMap the job data map
     * @return the job detail
     */
    JobDetail createJobDetail(Class<? extends Job> jobClass, String identity, String group, JobDataMap jobDataMap);

    /**
     * Create job detail job detail.
     *
     * @param jobClass      the job class
     * @param identity      the identity
     * @param group         the group
     * @param singleJobData the single job data
     * @return the job detail
     */
    JobDetail createJobDetail(Class<? extends Job> jobClass, String identity, String group, SingleJobData singleJobData);

    /**
     * Create job trigger trigger.
     *
     * @param jobDetail       the job detail
     * @param identity        the identity
     * @param group           the group
     * @param scheduleBuilder the schedule builder
     * @param startAt         the start at
     * @return the trigger
     */
    Trigger createJobTrigger(JobDetail jobDetail, String identity, String group, ScheduleBuilder<?> scheduleBuilder, Date startAt);

    /**
     * Create job trigger trigger.
     *
     * @param jobDetail       the job detail
     * @param identity        the identity
     * @param group           the group
     * @param scheduleBuilder the schedule builder
     * @return the trigger
     */
    Trigger createJobTrigger(JobDetail jobDetail, String identity, String group, ScheduleBuilder<?> scheduleBuilder);

    /**
     * Create simple schedule builder simple schedule builder.
     *
     * @param jobDetail the job detail
     * @param identity  the identity
     * @param group     the group
     * @param duration  the duration
     * @return the simple schedule builder
     */
    SimpleScheduleBuilder createSimpleScheduleBuilder(JobDetail jobDetail, String identity, String group, Duration duration);
}
