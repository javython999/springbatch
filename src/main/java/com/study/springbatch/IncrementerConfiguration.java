package com.study.springbatch;

import lombok.RequiredArgsConstructor;
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
public class IncrementerConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job incrementerJob() {
        return new JobBuilder("incrementerJob", jobRepository)
                .start(step1())
                .next(step2())
                .incrementer(new CustomJobParametersIncrement())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("incrementerJob-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("incrementerJob-step1 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("incrementerJob-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("incrementerJob-step2 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}
