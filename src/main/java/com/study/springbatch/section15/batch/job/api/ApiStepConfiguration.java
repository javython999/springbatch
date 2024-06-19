package com.study.springbatch.section15.batch.job.api;

import com.study.springbatch.section15.batch.chunk.processor.ApiItemProcessor1;
import com.study.springbatch.section15.batch.chunk.processor.ApiItemProcessor2;
import com.study.springbatch.section15.batch.chunk.processor.ApiItemProcessor3;
import com.study.springbatch.section15.batch.chunk.writer.ApiItemWirter1;
import com.study.springbatch.section15.batch.chunk.writer.ApiItemWirter2;
import com.study.springbatch.section15.batch.chunk.writer.ApiItemWirter3;
import com.study.springbatch.section15.batch.classifier.ProcessorClassifier;
import com.study.springbatch.section15.batch.classifier.WriterClassifier;
import com.study.springbatch.section15.batch.domain.ApiRequestVO;
import com.study.springbatch.section15.batch.domain.ProductVO;
import com.study.springbatch.section15.batch.partition.ProductPartitioner;
import com.study.springbatch.section15.batch.service.ApiService1;
import com.study.springbatch.section15.batch.service.ApiService2;
import com.study.springbatch.section15.batch.service.ApiService3;
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
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    private final ApiService1 apiService1;
    private final ApiService2 apiService2;
    private final ApiService3 apiService3;

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
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setThreadNamePrefix("api-thread");
        return executor;
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
    public ItemProcessor itemProcessor() throws Exception {
        ClassifierCompositeItemProcessor<ProductVO, ApiRequestVO> processor = new ClassifierCompositeItemProcessor<ProductVO, ApiRequestVO>();

        ProcessorClassifier<ProductVO, ItemProcessor<?, ? extends ApiRequestVO>> classifier = new ProcessorClassifier();
        Map<String, ItemProcessor<ProductVO, ApiRequestVO>> processorMap = new HashMap<>();
        processorMap.put("1", new ApiItemProcessor1());
        processorMap.put("2", new ApiItemProcessor2());
        processorMap.put("3", new ApiItemProcessor3());

        classifier.setProcessorsMap(processorMap);

        processor.setClassifier(classifier);

        return processor;
    }

    @Bean
    public ItemWriter<ApiRequestVO> itemWriter() throws Exception {
        ClassifierCompositeItemWriter<ApiRequestVO> writer = new ClassifierCompositeItemWriter<>();

        WriterClassifier<ApiRequestVO, ItemWriter<? super ApiRequestVO>> classifier = new WriterClassifier<>();

        Map<String, ItemWriter<ApiRequestVO>> writerMap = new HashMap<>();

        writerMap.put("1", new ApiItemWirter1(apiService1));
        writerMap.put("2", new ApiItemWirter2(apiService2));
        writerMap.put("3", new ApiItemWirter3(apiService3));

        classifier.setWriterMap(writerMap);

        writer.setClassifier(classifier);

        return writer;
    }

}
