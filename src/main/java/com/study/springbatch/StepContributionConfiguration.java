
package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class StepContributionConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job StepContributionConfig() {
        return new JobBuilder("StepContributionConfig", jobRepository)
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("StepContributionConfig-Step1", jobRepository)
                .tasklet(new logTasklet("StepContributionConfig-step1 EXECUTED"), tx).build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("StepContributionConfig-Step2", jobRepository)
                .tasklet(new logTasklet("StepContributionConfig-step2 EXECUTED"), tx).build();
    }
}
