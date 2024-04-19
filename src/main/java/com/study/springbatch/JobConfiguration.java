package com.study.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
public class JobConfiguration {

    //@Bean
    public Job batchJob1(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("batchJob1", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public Job batchJob2(JobRepository jobRepository, Step step3, Step step4, Step step5) {
        return new JobBuilder("batchJob2", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(flow(step3, step4))
                .next(step5)
                .end()
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobConfiguration-step1", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step1 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobConfiguration-step2", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step2 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Flow flow(Step step3, Step step4) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
        flowBuilder.start(step3)
                .next(step4)
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Step step3(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobConfiguration-step3", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step3 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step4(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobConfiguration-step4", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step4 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step5(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobConfiguration-step5", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step5 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }
}
