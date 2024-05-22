package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FlatFileItemWriterConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job job() {

        LocalDateTime now = LocalDateTime.now();

        return new JobBuilder("FlatFileItemWriter - " + now, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }



    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<FlatFileItem, FlatFileItem>chunk(5, tx)
                .reader(customItemReader())
                .writer(customItemWriter())
                .build();
    }


    @Bean
    public ItemReader<? extends FlatFileItem> customItemReader() {
        List<FlatFileItem> flatFileItems = Arrays.asList(
                    new FlatFileItem(1, "item1", 10),
                    new FlatFileItem(2, "item2", 20),
                    new FlatFileItem(3, "item3", 30)
                );

        return new ListItemReader<>(flatFileItems);
        //return new ListItemReader<>(Collections.emptyList());
    }

    @Bean
    public ItemWriter<? super FlatFileItem> customItemWriter() {
        return new FlatFileItemWriterBuilder<FlatFileItem>()
                .name("itemWriter")
                .resource(new FileSystemResource("F:\\IdeaProject\\springbatch\\src\\main\\resources\\flatfile.txt"))
                .append(true)
                .shouldDeleteIfEmpty(true)
                .delimited()
                .delimiter("|")
                .names(new String[] {"id", "name", "age"})
                .build();
    }
}
