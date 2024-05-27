package com.study.springbatch.section8.db;

import com.study.springbatch.vo.Customer2;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;

//@Configuration
@RequiredArgsConstructor
public class JdbcCursorItemReaderConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final DataSource dataSource;
    private int chunkSize = 10;

    @Bean
    public Job job() {
        LocalDateTime now = LocalDateTime.now();
        return new JobBuilder("batchJob " + now, jobRepository)
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<Customer2, Customer2>chunk(chunkSize, tx)
                .reader(customItemReader())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer2> customItemReader() {
        return new JdbcCursorItemReaderBuilder<Customer2>()
                .name("jdbcCursorItemReader")
                .fetchSize(chunkSize)
                .sql("select id, firstName, lastName, birthdate from customer where firstName like ? order by lastName, firstName")
                .beanRowMapper(Customer2.class)
                .queryArguments("A%")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public ItemWriter<Customer2> customItemWriter() {
        return items -> {
          for (Customer2 item : items) {
              System.out.println(item);
          }
        };
    }
}
