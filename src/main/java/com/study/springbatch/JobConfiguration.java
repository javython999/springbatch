package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    @Bean
    public Job job(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("JobConfiguration-job", jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobConfiguration-step1", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("step1 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobConfiguration-step2", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("step2 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }
}
