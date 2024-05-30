package com.study.springbatch.section11.faulttolerant;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class FaultTolerantConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private int chunkSize = 3;
    private int skipLimit = 2;
    private int retryLimit = 2;


    @Bean
    public Job job() {
        return new JobBuilder("FaultTolerantJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<String, String>chunk(chunkSize, tx)
                .reader(new ItemReader<String>() {
                    int i = 0;

                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;

                        if (i == 1) {
                            throw new IllegalArgumentException("this exception is skipped");
                        }
                        return i > 3 ? null : "item" + i;
                    }
                })
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        throw new IllegalStateException("this exception is retried");
                        //return "";
                    }
                })
                .writer(items -> items.forEach(System.out::println))
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skipLimit(skipLimit)
                .retry(IllegalStateException.class)
                .retryLimit(retryLimit)
                .build();
    }
}
