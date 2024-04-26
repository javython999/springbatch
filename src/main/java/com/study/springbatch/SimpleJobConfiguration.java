package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class SimpleJobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job batchJob() {
        return new JobBuilder("SimpleJobConfiguration", jobRepository)
                .start(step1())
                .next(step2())
                .next(step3())
                .incrementer(new RunIdIncrementer())
                .validator(new JobParametersValidator() {
                    @Override
                    public void validate(JobParameters parameters) throws JobParametersInvalidException {

                    }
                })
                .preventRestart()
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        JobExecutionListener.super.beforeJob(jobExecution);
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        JobExecutionListener.super.afterJob(jobExecution);
                    }
                })
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("SimpleJobConfiguration-step1", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("SimpleJobConfiguration-step1 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("SimpleJobConfiguration-step2", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("SimpleJobConfiguration-step2 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step3() {
        return new StepBuilder("SimpleJobConfiguration-step3", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("SimpleJobConfiguration-step3 was executed");
                    chunkConext.getStepContext().getStepExecution().setStatus(BatchStatus.FAILED);
                    contribution.setExitStatus(ExitStatus.STOPPED);
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }
}
