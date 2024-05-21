package com.study.springbatch;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class ItemReaderAdapterConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final EntityManagerFactory entityManagerFactory;
    private int chunkSize = 5;

    @Bean
    public Job job() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        return new JobBuilder("batchJob " + now, jobRepository)
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() throws Exception {
        return new StepBuilder("step1", jobRepository)
                .<String, String>chunk(chunkSize, tx)
                .reader(customItemReader())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public ItemReader<String> customItemReader() {
        ItemReaderAdapter<String> reader = new ItemReaderAdapter<>();
        reader.setTargetObject(customService());
        reader.setTargetMethod("customRead");

        return reader;
    }

    @Bean
    public Object customService() {
        return new CustomService();
    }




    @Bean
    public ItemWriter<String> customItemWriter() {
        return items -> {
            System.out.println("-----------------------");
            for (String item : items) {
                System.out.println(item);
            }

        };
    }
}
