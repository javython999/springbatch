package com.study.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

//@Configuration
public class JobExecutionConfiguration {

    @Bean
    public Job JobExecution(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("JobExecution", jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }


    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobExecution-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("JobExecution-STEP1 EXECUTED");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }


    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobExecution-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("JobExecution-STEP2 EXECUTED");
                    //throw new RuntimeException("step2 has failed");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }
}
