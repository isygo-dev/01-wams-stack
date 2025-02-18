package eu.isygoit.quartz.service;

import eu.isygoit.quartz.types.SingleJobData;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.utils.Key;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

/**
 * Enhanced QuartzService implementation using Java 17 features.
 * Provides methods to manage Quartz job scheduling components.
 */
@Slf4j
@Service
public class QuartzServiceImpl implements QuartzService {

    @Override
    public CronScheduleBuilder createCronScheduleBuilder(JobDetail jobDetail, String identity, String group, CronExpression cronExpression) {
        var jobIdentity = new JobIdentity(identity, group);
        log.debug("Creating CronScheduleBuilder for job [{}:{}] with expression: {}",
                jobIdentity.identity(), jobIdentity.group(), cronExpression);
        return CronScheduleBuilder.cronSchedule(cronExpression);
    }

    @Override
    public CronScheduleBuilder createCronScheduleBuilder(JobDetail jobDetail, String identity, String group, String cron) {
        var jobIdentity = new JobIdentity(identity, group);
        log.debug("Creating CronScheduleBuilder for job [{}:{}] with cron: {}",
                jobIdentity.identity(), jobIdentity.group(), cron);
        return CronScheduleBuilder.cronSchedule(cron);
    }

    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, JobDataMap jobDataMap) {
        log.debug("Creating job detail for class: {} with {} data entries",
                jobClass.getSimpleName(), jobDataMap.size());
        return JobBuilder.newJob(jobClass)
                .withIdentity(Key.createUniqueName(null))
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, String identity, String group, JobDataMap jobDataMap) {
        var jobIdentity = new JobIdentity(identity, group);
        log.debug("Creating job detail [{}:{}] for class: {} with {} data entries",
                jobIdentity.identity(), jobIdentity.group(), jobClass.getSimpleName(), jobDataMap.size());
        return JobBuilder.newJob(jobClass)
                .withIdentity(identity, group)
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, String identity, String group, SingleJobData singleJobData) {
        return Optional.ofNullable(singleJobData)
                .map(data -> {
                    log.debug("Creating job detail [{}:{}] with single job data key: {}",
                            identity, group, data.getKey());
                    return createJobDetailWithValue(jobClass, identity, group, data.getKey(), data.getValue());
                })
                .orElseGet(() -> {
                    log.error("Failed to create job detail - SingleJobData is null");
                    return null;
                });
    }

    /**
     * Creates a job detail based on the type of value provided.
     * Optimized version with better type handling and reduced complexity.
     *
     * @param jobClass Job class to be executed
     * @param identity Job identity name
     * @param group    Job group name
     * @param key      Job data key
     * @param value    Job data value
     * @return JobDetail instance or null if value type is unsupported
     */
    private JobDetail createJobDetailWithValue(Class<? extends Job> jobClass, String identity, String group, String key, Object value) {
        JobBuilder jobBuilder = JobBuilder.newJob(jobClass).withIdentity(identity, group).storeDurably();

        if (value instanceof String data) {
            jobBuilder.usingJobData(key, data);
        } else if (value instanceof Integer data) {
            jobBuilder.usingJobData(key, data);
        } else if (value instanceof Float data) {
            jobBuilder.usingJobData(key, data);
        } else if (value instanceof Long data) {
            jobBuilder.usingJobData(key, data);
        } else if (value instanceof Double data) {
            jobBuilder.usingJobData(key, data);
        } else if (value instanceof Boolean data) {
            jobBuilder.usingJobData(key, data);
        } else if (value instanceof JobDataMap data) {
            jobBuilder.usingJobData(data);
        } else {
            log.error("Job data type {} is invalid, job will not be created", value.getClass().getSimpleName());
            return null;
        }

        return jobBuilder.build();
    }

    /**
     * Creates a job detail based on the type of value provided.
     * Uses Java 17 enhanced instanceof pattern matching for type checking.
     */

    @Override
    public Trigger createJobTrigger(JobDetail jobDetail, String identity, String group,
                                    ScheduleBuilder<?> scheduleBuilder, Date startAt) {
        var jobIdentity = new JobIdentity(identity, group);
        log.info("Creating triggered job [{}:{}] scheduled to start at: {}",
                jobIdentity.identity(), jobIdentity.group(), startAt);

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(identity, group)
                .withSchedule(scheduleBuilder)
                .startAt(startAt)
                .build();
    }

    @Override
    public Trigger createJobTrigger(JobDetail jobDetail, String identity, String group,
                                    ScheduleBuilder<?> scheduleBuilder) {
        var jobIdentity = new JobIdentity(identity, group);
        log.debug("Creating immediate trigger for job [{}:{}]",
                jobIdentity.identity(), jobIdentity.group());

        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(identity, group)
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Override
    public SimpleScheduleBuilder createSimpleScheduleBuilder(JobDetail jobDetail, String identity,
                                                             String group, Duration duration) {
        var jobIdentity = new JobIdentity(identity, group);
        var seconds = duration.getSeconds();

        log.debug("Creating SimpleScheduleBuilder for job [{}:{}] with interval: {} seconds",
                jobIdentity.identity(), jobIdentity.group(), seconds);

        return SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds((int) seconds)
                .repeatForever();
    }

    // Record for job identity information to ensure immutability
    private record JobIdentity(String identity, String group) {
    }
}