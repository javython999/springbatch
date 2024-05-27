package com.study.springbatch.section5;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

//@Configuration
@RequiredArgsConstructor
public class TaskletStepConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job taskletJob() {
        return new JobBuilder("taskletJob", jobRepository)
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("taskletJob-step1", jobRepository)
                .tasklet(new CustomTasklet(), tx).build();
    }

   @Bean
    public Step step2() {
        return new StepBuilder("taskletJob-step2", jobRepository)
                .chunk(10, tx)
                .reader(new ListItemReader<>(Arrays.asList("item1","item2","item3","item4","item5")))
                .processor(item -> item.toString().toUpperCase())
                .writer(System.out::println)
                .build();
    }

}
