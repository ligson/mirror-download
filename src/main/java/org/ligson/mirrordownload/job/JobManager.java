package org.ligson.mirrordownload.job;

import org.ligson.mirrordownload.config.vo.AppConfig;
import org.quartz.*;

public class JobManager {

    private AppConfig appConfig;

    public JobManager(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void run() throws SchedulerException {
        SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();

        // 创建 JobDataMap，并设置参数
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("appConfig", appConfig);

        JobDetail jobDetail = JobBuilder.newJob(CrawlerJob.class)
                .withIdentity("weeklyJob", "group1")
                .usingJobData(jobDataMap)
                .build();

        // 使用 Cron 表达式安排每周日晚上的1点执行
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("weeklyTrigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule(appConfig.getApp().getJob().getCron())) // 每周日1点
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        scheduler.start();
    }
}
