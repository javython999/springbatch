package com.study.springbatch.section9.itemWriterAdapter;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

//@Configuration
@RequiredArgsConstructor
public class ItemWriterAdapterConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final int chunkSize = 10;

    @Bean
    public Job job() {
        LocalDateTime now = LocalDateTime.now();
        return new JobBuilder("itemWriterAdapter - " + now, jobRepository)
                .start(step1())
                .build();
    }


    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<String, String>chunk(chunkSize, tx)
                .reader(new ItemReader<String>() {

                    int index = 0;

                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        index++;
                        return index > 10 ? null : "item" + index;
                    }
                })
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super String> customItemWriter() {
        ItemWriterAdapter<String> writer = new ItemWriterAdapter<>();
        writer.setTargetObject(customService());
        writer.setTargetMethod("customWrite");
        return writer;
    }

    @Bean
    public CustomService customService() {
        return new CustomService();
    }
}
