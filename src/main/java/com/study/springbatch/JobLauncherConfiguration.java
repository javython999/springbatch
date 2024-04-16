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

@Configuration
public class JobLauncherConfiguration {

    @Bean
    public Job JobLauncher(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("JobLauncherConfiguration", jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobLauncherConfiguration-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(3000);
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobLauncherConfiguration-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }
}
