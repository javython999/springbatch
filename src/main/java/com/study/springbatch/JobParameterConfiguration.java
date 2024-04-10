package com.study.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

//@Configuration
public class JobParameterConfiguration {

    @Bean
    public Job JobParameter(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("JobParameter", jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobParameter-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    //contribution.getStepExecution().getJobExecution().getJobParameters()
                    JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
                    jobParameters.getString("name");
                    jobParameters.getLong("seq");
                    jobParameters.getDate("date");
                    jobParameters.getDouble("avg");


                    Map<String, Object> parametersMap = chunkContext.getStepContext().getJobParameters();
                    parametersMap.get("name");
                    parametersMap.get("seq");
                    parametersMap.get("date");
                    parametersMap.get("avg");


                    System.out.println("STEP1 EXECUTED");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }

    @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("JobParameter-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("STEP2 EXECUTED");
                    return RepeatStatus.FINISHED;
                }, tx).build();
    }
}
