package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@RequiredArgsConstructor
public class FlowStepConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job flowStepJob() {
        return new JobBuilder("flowStepJob", jobRepository)
                .start(flowStep())
                .next(step2())
                .build();
    }

    @Bean
    public Step flowStep() {
        return new StepBuilder("flowStep", jobRepository)
                .flow(flow())
                .build();
    }

    @Bean
    public Flow flow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
        flowBuilder.start(step1())
                .end();

        return flowBuilder.build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("flowStep-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 was executed");
                    throw new RuntimeException("step1 was Failed");
                    //return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("flowStep-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}

