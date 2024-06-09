package com.study.springbatch.section12.partitioning;

import com.study.springbatch.section12.async.AsyncItem;
import com.study.springbatch.section12.async.AsyncItemDto;
import com.study.springbatch.section12.async.AsyncItemRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PartitioningConfiguration {

    private final DataSource dataSource;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job job() {
        return new JobBuilder("PartitioningJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(masterStep())
                .build();
    }

    @Bean
    public Step masterStep() {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step slaveStep() {
        return new StepBuilder("slaveStep", jobRepository)
                .<AsyncItemDto, AsyncItemDto>chunk(100, tx)
                .reader(pagingItemReader(null, null))
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public Partitioner partitioner() {
        ColumnRangePartitioner partitioner = new ColumnRangePartitioner();
        partitioner.setColumn("id");
        partitioner.setTable("customer");
        partitioner.setDataSource(dataSource);
        return partitioner;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<AsyncItemDto> pagingItemReader(
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue
    ) {
        JdbcPagingItemReader<AsyncItemDto> reader = new JdbcPagingItemReader<>();
        log.info("Initializing reader for range: {} - {}", minValue, maxValue);

        reader.setDataSource(dataSource);
        reader.setFetchSize(100);
        reader.setRowMapper(new AsyncItemRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
        queryProvider.setWhereClause("where id >= " + minValue + " AND id <= " + maxValue);

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);
        return reader;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter customItemWriter() {
        JdbcBatchItemWriter<AsyncItem> writer = new JdbcBatchItemWriter<>();
        writer.setDataSource(dataSource);
        writer.setSql("insert into async_item values(:id, :firstName, :lastName, :birthDate)");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();
        return writer;
    }
}
