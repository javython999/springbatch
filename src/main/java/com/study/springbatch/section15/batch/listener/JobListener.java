package com.study.springbatch.section15.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;

public class JobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        JobExecutionListener.super.beforeJob(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
        System.out.println("총 소요시간 = " + duration.getSeconds());
    }
}
