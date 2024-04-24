package com.study.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@Configuration
public class TaskletStepConfiguration {

    @Bean
    public Job taskletJob(JobRepository jobRepository, Step step1, Step step2) {
        return new JobBuilder("taskletJob", jobRepository)
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("taskletJob-step1", jobRepository)
                .tasklet(new CustomTasklet(), tx).build();
    }

   @Bean
    public Step step2(JobRepository jobRepository, PlatformTransactionManager tx) {
        return new StepBuilder("taskletJob-step2", jobRepository)
                .chunk(10, tx)
                .reader(new ListItemReader<>(Arrays.asList("item1","item2","item3","item4","item5")))
                .processor(item -> item.toString().toUpperCase())
                .writer(System.out::println)
                .build();
    }

}
