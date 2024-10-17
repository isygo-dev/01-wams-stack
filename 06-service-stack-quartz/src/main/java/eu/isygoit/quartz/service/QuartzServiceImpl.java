package eu.isygoit.quartz.service;

import eu.isygoit.quartz.types.SingleJobData;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.utils.Key;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * The type Quartz service.
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
            Object o = singleJobData.getValue();
            if (o instanceof String) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), (String) singleJobData.getValue()).storeDurably().build();
            } else if (o instanceof Integer) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), (Integer) singleJobData.getValue()).storeDurably().build();
            } else if (o instanceof Long) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), (Long) singleJobData.getValue()).storeDurably().build();
            } else if (o instanceof Double) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), (Double) singleJobData.getValue()).storeDurably().build();
            } else if (o instanceof Boolean) {
                return JobBuilder.newJob(jobClass).withIdentity(identity, group).usingJobData(singleJobData.getKey(), (Boolean) singleJobData.getValue()).storeDurably().build();
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
