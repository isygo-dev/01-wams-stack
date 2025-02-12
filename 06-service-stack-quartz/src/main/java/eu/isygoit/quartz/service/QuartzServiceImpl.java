package eu.isygoit.quartz.service;

import eu.isygoit.quartz.types.SingleJobData;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.*;
import org.quartz.utils.Key;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

/**
 * The QuartzService implementation.
 * Provides methods to create job details, triggers, and schedule builders.
 */
@Slf4j
@Service
public class QuartzServiceImpl implements QuartzService {

    /**
     * Creates a cron schedule builder using a CronExpression.
     *
     * @param jobDetail      the job detail
     * @param identity       the identity of the job
     * @param group          the group of the job
     * @param cronExpression the cron expression
     * @return the cron schedule builder
     */
    @Override
    public CronScheduleBuilder createCronScheduleBuilder(JobDetail jobDetail, String identity, String group, CronExpression cronExpression) {
        log.debug("Creating CronScheduleBuilder with expression: {}", cronExpression);
        return CronScheduleBuilder.cronSchedule(cronExpression);
    }

    /**
     * Creates a cron schedule builder using a cron expression in string format.
     *
     * @param jobDetail the job detail
     * @param identity  the identity of the job
     * @param group     the group of the job
     * @param cron      the cron string
     * @return the cron schedule builder
     */
    @Override
    public CronScheduleBuilder createCronScheduleBuilder(JobDetail jobDetail, String identity, String group, String cron) {
        log.debug("Creating CronScheduleBuilder with cron string: {}", cron);
        return CronScheduleBuilder.cronSchedule(cron);
    }

    /**
     * Creates a job detail with the given job class and job data map.
     *
     * @param jobClass   the job class
     * @param jobDataMap the job data map
     * @return the job detail
     */
    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, JobDataMap jobDataMap) {
        log.debug("Creating job detail for class: {}", jobClass.getSimpleName());
        return JobBuilder.newJob(jobClass).withIdentity(Key.createUniqueName(null)).usingJobData(jobDataMap).storeDurably().build();
    }

    /**
     * Creates a job detail with the given identity, group, and job data map.
     *
     * @param jobClass   the job class
     * @param identity   the identity of the job
     * @param group      the group of the job
     * @param jobDataMap the job data map
     * @return the job detail
     */
    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, String identity, String group, JobDataMap jobDataMap) {
        log.debug("Creating job detail with identity: {} and group: {}", identity, group);
        return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(jobDataMap).storeDurably().build();
    }

    /**
     * Creates a job detail using SingleJobData, with type-specific handling.
     *
     * @param jobClass      the job class
     * @param identity      the identity of the job
     * @param group         the group of the job
     * @param singleJobData the single job data containing key-value pair
     * @return the job detail or null if data is invalid
     */
    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, String identity, String group, SingleJobData singleJobData) {
        return Optional.ofNullable(singleJobData)
                .map(data -> createJobDetailWithValue(jobClass, identity, group, singleJobData.getKey(), data.getValue()))
                .orElseGet(() -> {
                    log.error("Job data is null, job will not be created");
                    return null;
                });
    }

    /**
     * Creates a job detail based on a value from SingleJobData.
     *
     * @param jobClass the job class
     * @param identity the identity of the job
     * @param group    the group of the job
     * @param key      the job data key
     * @param value    the value of the job data
     * @return the job detail
     */
    private JobDetail createJobDetailWithValue(Class<? extends Job> jobClass, String identity, String group, String key, Object value) {
        if (value instanceof String data) {
            return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(key, data).storeDurably().build();
        } else if (value instanceof Integer data) {
            return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(key, data).storeDurably().build();
        } else if (value instanceof Float data) {
            return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(key, data).storeDurably().build();
        } else if (value instanceof Long data) {
            return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(key, data).storeDurably().build();
        } else if (value instanceof Double data) {
            return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(key, data).storeDurably().build();
        } else if (value instanceof Boolean data) {
            return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(key, data).storeDurably().build();
        } else if (value instanceof JobDataMap data) {
            return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(data).storeDurably().build();
        } else {
            log.error("Job data type {} is invalid, job will not be created", value.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Creates a job trigger with the specified schedule and start time.
     *
     * @param jobDetail       the job detail
     * @param identity        the identity of the job
     * @param group           the group of the job
     * @param scheduleBuilder the schedule builder
     * @param startAt         the start date of the job
     * @return the trigger
     */
    @Override
    public Trigger createJobTrigger(JobDetail jobDetail, String identity, String group, ScheduleBuilder scheduleBuilder, Date startAt) {
        log.info("Job {} triggered and will start on {}", identity, startAt);
        return TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(identity, group).withSchedule(scheduleBuilder).startAt(startAt).build();
    }

    /**
     * Creates a job trigger with the specified schedule.
     *
     * @param jobDetail       the job detail
     * @param identity        the identity of the job
     * @param group           the group of the job
     * @param scheduleBuilder the schedule builder
     * @return the trigger
     */
    @Override
    public Trigger createJobTrigger(JobDetail jobDetail, String identity, String group, ScheduleBuilder scheduleBuilder) {
        log.debug("Creating job trigger for job: {} in group: {}", identity, group);
        return TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(identity, group).withSchedule(scheduleBuilder).build();
    }

    /**
     * Creates a simple schedule builder with a fixed interval in seconds.
     *
     * @param jobDetail the job detail
     * @param identity  the identity of the job
     * @param group     the group of the job
     * @param duration  the interval duration
     * @return the simple schedule builder
     */
    @Override
    public SimpleScheduleBuilder createSimpleScheduleBuilder(JobDetail jobDetail, String identity, String group, Duration duration) {
        log.debug("Creating SimpleScheduleBuilder with interval: {} seconds", duration.getSeconds());
        return SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds((int) duration.getSeconds()).repeatForever();
    }
}
