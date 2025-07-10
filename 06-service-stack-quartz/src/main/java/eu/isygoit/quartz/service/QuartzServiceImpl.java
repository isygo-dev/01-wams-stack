package eu.isygoit.quartz.service;

import eu.isygoit.quartz.types.SingleJobData;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.utils.Key;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * The type Quartz api.
 */
@Slf4j
@Service
public class QuartzServiceImpl implements QuartzService {

    @Override
    public CronScheduleBuilder createCronScheduleBuilder(JobDetail jobDetail, String identity, String group, CronExpression cronExpression) {
        return CronScheduleBuilder.cronSchedule(cronExpression);
    }

    @Override
    public CronScheduleBuilder createCronScheduleBuilder(JobDetail jobDetail, String identity, String group, String cron) {
        return CronScheduleBuilder.cronSchedule(cron);
    }

    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, JobDataMap jobDataMap) {
        return JobBuilder.newJob(jobClass).withIdentity(Key.createUniqueName(null)).usingJobData(jobDataMap).storeDurably().build();
    }

    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, String identity, String group, JobDataMap jobDataMap) {
        return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(jobDataMap).storeDurably().build();
    }

    @Override
    public JobDetail createJobDetail(Class<? extends Job> jobClass, String identity, String group, SingleJobData singleJobData) {
        if (singleJobData.getValue() != null) {
            Object value = singleJobData.getValue();
            if (value instanceof String data) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), data).storeDurably().build();
            } else if (value instanceof Integer data) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), data).storeDurably().build();
            } else if (value instanceof Long data) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), data).storeDurably().build();
            } else if (value instanceof Double data) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), data).storeDurably().build();
            } else if (value instanceof Boolean data) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), data).storeDurably().build();
            } else {
                log.error("<ERROR>: Job data type is invalid, job will not be created");
            }
        } else {
            log.error("<ERROR>: Job data is null, job will not be created");
        }
        return null;
    }

    @Override
    public Trigger createJobTrigger(JobDetail jobDetail, String identity, String group, ScheduleBuilder scheduleBuilder, Date startAt) {
        log.info("Job {} triggered and will start on {}", identity, startAt);
        return TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(identity, group).withSchedule(scheduleBuilder)
                .startAt(startAt).build();
    }

    @Override
    public Trigger createJobTrigger(JobDetail jobDetail, String identity, String group, ScheduleBuilder scheduleBuilder) {
        return TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(identity, group).withSchedule(scheduleBuilder)
                .build();
    }

    @Override
    public SimpleScheduleBuilder createSimpleScheduleBuilder(JobDetail jobDetail, String identity, String group, Duration duration) {
        return SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds((int) duration.getSeconds()).repeatForever();
    }
}
