package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//@Configuration
@RequiredArgsConstructor
public class JdbcPagingItemReaderConfiguration {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final DataSource dataSource;
    private int chunkSize = 2;

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
                .<Customer, Customer>chunk(chunkSize, tx)
                .reader(customItemReader())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer> customItemReader() throws Exception {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("firstname", "A%");

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("jdbcPagingItemReader")
                .pageSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .queryProvider(createQueryProvider())
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {

        SqlPagingQueryProviderFactoryBean sqlPagingQueryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        sqlPagingQueryProviderFactoryBean.setDataSource(dataSource);
        sqlPagingQueryProviderFactoryBean.setSelectClause("id,firstname,lastname,birthdate");
        sqlPagingQueryProviderFactoryBean.setFromClause("from Customer");
        sqlPagingQueryProviderFactoryBean.setWhereClause("where firstname like :firstname");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        sqlPagingQueryProviderFactoryBean.setSortKeys(sortKeys);

        return sqlPagingQueryProviderFactoryBean.getObject();
    }

    @Bean
    public ItemWriter<Customer> customItemWriter() {
        return items -> {
            System.out.println("--------------------");
            for (Customer item : items) {
                 System.out.println(item);
            }
        };
    }
}
