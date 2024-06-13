package com.study.springbatch.section13.skipretry;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RetryConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job job() {
        return new JobBuilder("retryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<Integer, String>chunk(10, tx)
                .reader(listItemReader())
                .processor(new CustomItemProcessor())
                .writer(new CustomItemWriter())
                .faultTolerant()
                .retry(CustomRetryException.class)
                .retryLimit(2)
                .listener(new CustomRetryListener())
                .build();
    }

    @Bean
    public ItemReader<Integer> listItemReader() {
        List<Integer> list = Arrays.asList(1,2,3,4);
        return new LinkedListItemReader2<>(list);
    }


}
