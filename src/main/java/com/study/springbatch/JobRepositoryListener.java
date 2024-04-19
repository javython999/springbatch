package com.study.springbatch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class JobRepositoryListener implements JobExecutionListener {

    //@Autowired
    JobRepository jobRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        JobExecutionListener.super.beforeJob(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        JobExecutionListener.super.afterJob(jobExecution);


        String jobName = jobExecution.getJobInstance().getJobName();

        JobParameters jobParameters = new JobParametersBuilder().addString("requestDate", "20240101").toJobParameters();

        JobExecution lastJobExecution = jobRepository.getLastJobExecution(jobName, jobParameters);

        if (lastJobExecution != null) {
            for (StepExecution stepExecution : lastJobExecution.getStepExecutions()) {
                System.out.println("status = " + stepExecution.getStatus());
                System.out.println("exit status = " + stepExecution.getExitStatus());
                System.out.println("step name = " + stepExecution.getStepName());
            }
        }
    }
}
