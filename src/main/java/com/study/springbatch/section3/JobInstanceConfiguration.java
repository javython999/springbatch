package com.study.springbatch.section3;

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
public class JobInstanceConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job jobInstnace() {
        return new JobBuilder("JobInstance", jobRepository)
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("jobinstance-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("STEP1 EXECUTED");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("jobinstance-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("STEP2 EXECUTED");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }
}
