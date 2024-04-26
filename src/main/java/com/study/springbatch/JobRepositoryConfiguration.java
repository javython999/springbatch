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
public class JobRepositoryConfiguration {

    private final JobRepositoryListener jobRepositoryListener;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job jobRepositoryJob() {
        return new JobBuilder("JobRepositoryConfiguration", jobRepository)
                .start(step1())
                .next(step2())
                .listener(jobRepositoryListener)
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("JobRepositoryConfiguration-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("JobRepositoryConfiguration-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

}
