package com.study.springbatch;

import com.study.springbatch.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

//@Configuration
@RequiredArgsConstructor
public class JsonConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job job() {
        LocalDateTime now = LocalDateTime.now();
        return new JobBuilder("XML batchJob " + now, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<Customer, Customer>chunk(3, tx)
                .reader(customItemReader())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public ItemReader<? extends Customer> customItemReader() {
        return new JsonItemReaderBuilder<Customer>()
                .name("staxXml")
                .resource(new ClassPathResource("customer.json"))
                .jsonObjectReader(new JacksonJsonObjectReader<>(Customer.class))
                .build();
    }


    @Bean
    public ItemWriter<Customer> customItemWriter() {
        return items -> {
            for (Customer customer : items) {
                System.out.println(customer);
            }
        };
    }
}
