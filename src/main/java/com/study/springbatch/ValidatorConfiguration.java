package com.study.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ValidatorConfiguration {

    @Bean
    public Job validatorJob(JobRepository jobRepository, Step step1, Step step2) {

        return new JobBuilder("ValidatorJob", jobRepository)
                .start(step1)
                .next(step2)
                //.validator(new CustomJobParametersValidator())
                .validator(new DefaultJobParametersValidator(new String[] {"name", "date"}, new String[] {"count"}))
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("ValidatorJob-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("ValidatorJob-step1 was executed");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("ValidatorJob-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("ValidatorJob-step2 was executed");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

}
