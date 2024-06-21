package com.study.springbatch.section15.scheduler;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

public abstract class JobRunner implements ApplicationRunner {
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        doRun(args);
    }

    protected abstract void doRun(ApplicationArguments args);

    public Trigger buildJobTrigger(String sheduleExp) {
        return TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(sheduleExp))
                .build();
    }

    public JobDetail buildJobDetail(Class job, String name, String group, Map param) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.putAll(param);

        return JobBuilder.newJob(job)
                .withIdentity(name, group)
                .usingJobData(jobDataMap)
                .build();
    }
}
