package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class CustomExitStatusConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job customExitStatusJob() {
        return new JobBuilder("CustomExitStatus-Job", jobRepository)
                .start(step1())
                    .on("FAILED")
                    .to(step2())
                    .on("PASS")
                    .stop()
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("CustomExitStatus-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("CustomExitStatus-step1 was executed");
                    contribution.getStepExecution().setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("CustomExitStatus-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("CustomExitStatus-step2 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .listener(new PassCheckingListener())
                .build();
    }
}
