package com.study.springbatch.section13.jobstepListener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;

import java.time.Duration;
import java.time.LocalDateTime;

public class CustomJobAnnotationExecutionListener {

    @BeforeJob
    public void startLog(JobExecution jobExecution) {
        System.out.println("Job is started");
        System.out.println("Job Name: " + jobExecution.getJobInstance().getJobName());
    }

    @AfterJob
    public void endLog(JobExecution jobExecution) {
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();
        Duration duration = Duration.between(startTime, endTime);
        System.out.println("duration: " + duration);
    }
}
