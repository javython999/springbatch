package com.study.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class StepConfiguration {

    @Bean
    public Job StepConfig(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("Step", jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("Step-Step1", jobRepository)
                .tasklet(new logTasklet("Step-step1 EXECUTED"), tx).build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("Step-Step2", jobRepository)
                .tasklet(new logTasklet("Step-step2 EXECUTED"), tx).build();
    }
}
