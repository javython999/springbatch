package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class LimitAllowConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job limitAllowJob() {
        return new JobBuilder("limitAllowJob", jobRepository)
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("limitAllow-Step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("limitAllow-Step1 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("limitAllow-Step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("limitAllow-Step2 was executed");
                    throw new RuntimeException("step2 failed");
                    //return RepeatStatus.FINISHED;
                }, tx)
                .startLimit(3)
                .build();
    }
}
