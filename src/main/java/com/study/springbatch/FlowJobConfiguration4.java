package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
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
public class FlowJobConfiguration4 {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;


    @Bean
    public Job flowJob() {
        return new JobBuilder("flowJob", jobRepository)
                .start(flow1())
                    .on("COMPLETED")
                    .to(flow2())
                .from(flow1())
                    .on("FAILED")
                    .to(flow3())
                .end()
                .build();
    }

    @Bean
    public Flow flow1() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow1");
        flowBuilder.start(step1())
                .next(step2())
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Flow flow2() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow2");
        flowBuilder.start(flow3())
                .next(step5())
                .next(step6())
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Flow flow3() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow3");
        flowBuilder.start(step3())
                .next(step4())
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("flowJob-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step1 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("flowJob-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step2 was executed");
                    throw new RuntimeException("FAILED");
                    //return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step3() {
        return new StepBuilder("flowJob-step3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step3 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step4() {
        return new StepBuilder("flowJob-step4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step4 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step5() {
        return new StepBuilder("flowJob-step5", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step5 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step6() {
        return new StepBuilder("flowJob-step6", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step6 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}
