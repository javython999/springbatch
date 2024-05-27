package com.study.springbatch.section10.compositeItemprocessor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

//@Configuration
@RequiredArgsConstructor
public class CompositeItemConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private int chunkSize = 10;

    @Bean
    public Job job() {
        return new JobBuilder("job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<String, String>chunk(chunkSize, tx)
                .reader(new ItemReader<>() {
                    int i = 0;

                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        return i > 10 ? null : "item" + i;
                    }
                })
                .processor(customItemProcessor())
                .writer(items -> items.forEach(item -> System.out.println(item)))
                .build();
    }

    @Bean
    public ItemProcessor<? super String, String> customItemProcessor() {

        List processors = new ArrayList<>();
        processors.add(new CustomItemProcessor1());
        processors.add(new CustomItemProcessor2());


        return new CompositeItemProcessorBuilder<>().delegates(processors).build();
    }
}
