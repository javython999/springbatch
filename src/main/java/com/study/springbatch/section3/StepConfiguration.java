package com.study.springbatch.section3;

import com.study.springbatch.tasklet.logTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class StepConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job StepConfig() {
        return new JobBuilder("Step", jobRepository)
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("Step-Step1", jobRepository)
                .tasklet(new logTasklet("Step-step1 EXECUTED"), tx).build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("Step-Step2", jobRepository)
                .tasklet(new logTasklet("Step-step2 EXECUTED"), tx).build();
    }
}
