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

@Configuration
@RequiredArgsConstructor
public class TransitionConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job transitionJob() {
        return new JobBuilder("TransitionJob", jobRepository)
                .start(step1())
                    .on("FAILED")
                    .to(step2())
                    .on("*")
                    .stop()
                .from(step1())
                    .on("*")
                    .to(step3())
                    .next(step4())
                    .on("FAILED")
                    .end()
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("TransitionJob-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("TransitionJob-step1 was executed");
                    return RepeatStatus.FINISHED;
                    //contribution.setExitStatus(ExitStatus.FAILED);
                    //throw new RuntimeException("error");
                }, tx)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("TransitionJob-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("TransitionJob-step2 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step3() {
        return new StepBuilder("TransitionJob-step3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("TransitionJob-step3 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step4() {
        return new StepBuilder("TransitionJob-step4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("TransitionJob-step4 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}
