package com.study.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class PreventRestartConfiguration {

    @Bean
    public Job preventRestartJob(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("preventRestartJob", jobRepository)
                .start(step1)
                .next(step2)
                .preventRestart()
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("preventRestartJob-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("preventRestartJob-step1 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("preventRestartJob-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    //throw new RuntimeException("임의 실패");
                    System.out.println("preventRestartJob-step2 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}
