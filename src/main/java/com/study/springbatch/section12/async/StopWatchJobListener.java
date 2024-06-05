package com.study.springbatch.section12.async;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.temporal.ChronoUnit;

public class StopWatchJobListener  implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        JobExecutionListener.super.beforeJob(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long secondsBetween = ChronoUnit.MILLIS.between(jobExecution.getStartTime(), jobExecution.getEndTime());
        System.out.println("=====================================================================");
        System.out.println("총 소요시간(millis) : " + secondsBetween);
        System.out.println("=====================================================================");
    }
}
