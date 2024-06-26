package com.study.springbatch.section6;

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
public class FlowJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job flowJob() {
        return new JobBuilder("flowJob", jobRepository)
                .start(step1()).on("COMPLETED").to(step3())
                .from(step1()).on("FAILED").to(step2())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("flowJob-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("flowJob-step1 was executed");
                    return RepeatStatus.FINISHED;
                    //throw new RuntimeException("step1 was failed");
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
