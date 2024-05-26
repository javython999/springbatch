package com.study.springbatch;

import com.study.springbatch.entity.Person;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

//@Configuration
@RequiredArgsConstructor
public class JpaPagingItemReaderConfiguration {
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
                .<Person, Person>chunk(chunkSize, tx)
                .reader(customItemReader())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public ItemReader<? extends Person> customItemReader() {

        return new JpaPagingItemReaderBuilder<Person>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("select p from Person p")
                .build();
    }



    @Bean
    public ItemWriter<Person> customItemWriter() {
        return items -> {
            System.out.println("--------------------");
            for (Person item : items) {
                 //System.out.println(item.getAddress().getLocation());
            }
            System.out.println("--------------------");
        };
    }
}
