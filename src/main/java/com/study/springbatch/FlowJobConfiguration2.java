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
public class FlowJobConfiguration2 {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job flowJob2() {
        return new JobBuilder("flowJob2", jobRepository)
                .start(flowA())
                .next(step3())
                .next(flowB())
                .next(step6())
                .end()
                .build();
    }

    @Bean
    public Flow flowA() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flowA");
        flowBuilder.start(step1())
                .next(step2())
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Flow flowB() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flowB");
        flowBuilder.start(step4())
                .next(step5())
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("flowJob2-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob2-step1 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("flowJob2-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob2-step2 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step3() {
        return new StepBuilder("flowJob2-step3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob2-step3 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step4() {
        return new StepBuilder("flowJob2-step4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob2-step4 was executed");
                    //return RepeatStatus.FINISHED;
                    throw new RuntimeException("step4 was failed");
                }, tx)
                .build();
    }

    @Bean
    public Step step5() {
        return new StepBuilder("flowJob2-step5", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob2-step5 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step6() {
        return new StepBuilder("flowJob2-step6", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob2-step6 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }



}
