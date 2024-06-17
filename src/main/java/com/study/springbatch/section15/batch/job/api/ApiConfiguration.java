package com.study.springbatch.section15.batch.job.api;

import com.study.springbatch.section15.batch.listener.JobListener;
import com.study.springbatch.section15.batch.tasklet.ApiEndTasklet;
import com.study.springbatch.section15.batch.tasklet.ApiStartTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class ApiConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final ApiStartTasklet apiStartTasklet;
    private final ApiEndTasklet apiEndTasklet;
    private final Step jobStep;

    @Bean
    public Job apiJob() {
        return new JobBuilder("apiJob", jobRepository)
                .listener(new JobListener())
                .start(apiStep1())
                .next(jobStep)
                .next(apiStep2())
                .build();
    }

    @Bean
    public Step apiStep1() {
        return new StepBuilder("apiStep1", jobRepository)
                .tasklet(apiStartTasklet, tx)
                .build();
    }

    @Bean
    public Step apiStep2() {
        return new StepBuilder("apiStep2", jobRepository)
                .tasklet(apiEndTasklet, tx)
                .build();
    }


}
