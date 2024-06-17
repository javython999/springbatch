package com.study.springbatch.section15.batch.job.api;

import com.study.springbatch.section15.batch.domain.ProductVO;
import com.study.springbatch.section15.batch.partition.ProductPartitioner;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ApiStepConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final DataSource dataSource;

    private int chunkSize = 10;

    @Bean
    public Step apiMasterStep() throws Exception {
        return new StepBuilder("apiMasterStep", jobRepository)
                .partitioner(apiSlaveStep().getName(), partitioner())
                .step(apiSlaveStep())
                .gridSize(3)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step apiSlaveStep() throws Exception {
        return new StepBuilder("apiSlaveStep", jobRepository)
                .<ProductVO, ProductVO>chunk(chunkSize, tx)
                .reader(itemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ProductPartitioner partitioner() {
        ProductPartitioner partitioner = new ProductPartitioner();
        partitioner.setDataSource(dataSource);
        return partitioner;
    }

    @Bean
    @StepScope
    public ItemReader<ProductVO> itemReader(@Value("#{stepExecutionContext['product']}") ProductVO productVO) throws Exception {
        JdbcPagingItemReader<ProductVO> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(chunkSize);
        reader.setRowMapper(new BeanPropertyRowMapper<>(ProductVO.class));

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, name, price, type");
        queryProvider.setFromClause("from product");
        queryProvider.setWhereClause("where type = :type");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.DESCENDING);

        queryProvider.setSortKeys(sortKeys);

        reader.setParameterValues(QueryGenerator.getParmaeterForQuery("tpye", productVO.getType()));
        reader.setQueryProvider(queryProvider);
        reader.afterPropertiesSet();

        return reader;
    }

    @Bean
    public ItemProcessor<ProductVO, ProductVO> itemProcessor() throws Exception {
        return null;
    }

    @Bean
    public ItemWriter<ProductVO> itemWriter() throws Exception {
        return null;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return null;
    }

}
