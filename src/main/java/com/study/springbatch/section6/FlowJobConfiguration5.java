package com.study.springbatch.section6;

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
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class FlowJobConfiguration5 {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;


    @Bean
    public Job flowJob() {
        return new JobBuilder("flowJob", jobRepository)
                .start(step1())
                    .on("COMPLETED")
                    .to(step2())
                .from(step1())
                    .on("FAILED")
                    .to(flow())
                .end()
                .build();
    }

    @Bean
    public Flow flow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow1");
        flowBuilder.start(step2())
                .on("*")
                .to(step3())
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("flowJob-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step1 was executed");
                    contribution.setExitStatus(ExitStatus.FAILED);
                    throw new RuntimeException("FAILED");
                    //return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("flowJob-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step2 was executed");
                    return RepeatStatus.FINISHED;
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


}
