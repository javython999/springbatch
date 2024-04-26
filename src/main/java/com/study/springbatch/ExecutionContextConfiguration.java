
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
public class ExecutionContextConfiguration {
    private final ExecutionContextTasklet1 executionContextTasklet1;
    private final ExecutionContextTasklet2 executionContextTasklet2;
    private final ExecutionContextTasklet3 executionContextTasklet3;
    private final ExecutionContextTasklet4 executionContextTasklet4;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job batchJob() {
        return new JobBuilder("ExecutionContextConfig", jobRepository)
                .start(step1())
                .next(step2())
                .next(step3())
                .next(step4())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("ExecutionContext-Step1", jobRepository)
                .tasklet(executionContextTasklet1, tx).build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("ExecutionContext-Step2", jobRepository)
                .tasklet(executionContextTasklet2, tx).build();
    }

    @Bean
    public Step step3() {
        return new StepBuilder("ExecutionContext-Step3", jobRepository)
                .tasklet(executionContextTasklet3, tx).build();
    }

    @Bean
    public Step step4() {
        return new StepBuilder("ExecutionContext-Step4", jobRepository)
                .tasklet(executionContextTasklet4, tx).build();
    }

}
