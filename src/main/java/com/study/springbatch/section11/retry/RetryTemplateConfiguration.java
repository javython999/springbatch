package com.study.springbatch.section11.retry;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//@Configuration
@RequiredArgsConstructor
public class RetryTemplateConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final int chunkSize = 5;

    @Bean
    public Job job() {
        return new JobBuilder("retryTemplateJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<String, RetryCustomer>chunk(chunkSize, tx)
                .reader(itemReader())
                .processor(itemProcssor())
                .writer(items -> items.forEach(System.out::println))
                .faultTolerant()
                .skip(RetryableException.class)
                .skipLimit(2)
                //.retry(RetryableException.class)
                //.retryLimit(2)
                .retryPolicy(retryPolicy())
                .build();
    }

    @Bean
    public ListItemReader<String> itemReader() {

        List<String> items = IntStream.range(0, 30)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        return new ListItemReader<>(items);
    }


    @Bean
    public ItemProcessor<? super String, RetryCustomer> itemProcssor() {
        return new RetryItemProcessor2();
    }

    @Bean
    public ExceptionHandler simpleLimitExceptionHandler() {
        return new SimpleLimitExceptionHandler(3);
    }

    @Bean
    public RetryPolicy retryPolicy() {
        Map<Class<? extends Throwable>, Boolean> exceptionClass = new HashMap<>();
        exceptionClass.put(RetryableException.class, true);
        return new SimpleRetryPolicy(2, exceptionClass);
    }

    @Bean
    public RetryTemplate retryTemplate() {
        Map<Class<? extends Throwable>, Boolean> exceptionClass = new HashMap<>();
        exceptionClass.put(RetryableException.class, true);

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(2000);

        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(2, exceptionClass);
        RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        //retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        return retryTemplate;
    }
}
