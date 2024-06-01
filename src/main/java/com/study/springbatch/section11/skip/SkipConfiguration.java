package com.study.springbatch.section11.skip;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

//@Configuration
@RequiredArgsConstructor
public class SkipConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private int chunkSize = 3;
    private int skipLimit = 5;


    @Bean
    public Job job() {
        return new JobBuilder("SkipJob", jobRepository)
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

                        if (i == 3) {
                            throw new SkippableException("skip");
                        }

                        System.out.println("itemReader = " + i);
                        return i > 20 ? null : String.valueOf(i);
                    }
                })
                .processor(itemProcessor())
                .writer(itemWriter())
                .faultTolerant()
                .skip(SkippableException.class)
                .noSkip(NonTransientResourceException.class)
                .skipLimit(skipLimit)
                //.skipPolicy(limitCheckingItemSkipPolicy())
                //.skipPolicy(new AlwaysSkipItemSkipPolicy())
                .build();
    }

    @Bean
    public ItemProcessor<? super String, ? extends String> itemProcessor() {
        return new SkipItemPorcessor();
    }

    @Bean
    public ItemWriter<? super String> itemWriter() {
        return new SkipItemWriter();
    }

    @Bean
    public SkipPolicy limitCheckingItemSkipPolicy() {
        Map<Class<? extends Throwable>, Boolean> policyMap = new HashMap<>();
        policyMap.put(SkippableException.class, true);
        return new LimitCheckingItemSkipPolicy(skipLimit, policyMap);
    }
}
