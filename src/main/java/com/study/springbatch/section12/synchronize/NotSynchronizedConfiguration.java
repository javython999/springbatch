package com.study.springbatch.section12.synchronize;

import com.study.springbatch.section12.async.AsyncItem;
import com.study.springbatch.section12.async.AsyncItemDto;
import com.study.springbatch.section12.async.StopWatchJobListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

//@Configuration
@RequiredArgsConstructor
public class NotSynchronizedConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final DataSource dataSource;

    @Bean
    public Job job() throws Exception {
        return new JobBuilder("job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Step step1() throws InterruptedException {
        return new StepBuilder("step1", jobRepository)
                .<AsyncItemDto, AsyncItem>chunk(20, tx)
                .reader(customItemReader())
                .listener(new ItemReadListener<AsyncItemDto>() {
                    @Override
                    public void beforeRead() {
                        ItemReadListener.super.beforeRead();
                    }

                    @Override
                    public void afterRead(AsyncItemDto item) {
                        System.out.println("Thread = " + Thread.currentThread().getName() +  " | item id = " + item.getId());
                    }

                    @Override
                    public void onReadError(Exception ex) {
                        ItemReadListener.super.onReadError(ex);
                    }
                })
                .writer(customItemWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<AsyncItemDto> customItemReader() {
        return new JdbcCursorItemReaderBuilder<AsyncItemDto>()
                .fetchSize(100)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(AsyncItemDto.class))
                .sql("select id, firstName, lastName, birthdate from customer")
                .name("NotThreadSafetyReader")
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<AsyncItem> customItemWriter() {
        JdbcBatchItemWriter<AsyncItem> itemWriter = new JdbcBatchItemWriter<>();

        itemWriter.setDataSource(dataSource);
        itemWriter.setSql("insert into async_item values(:id, :firstName, :lastName, :birthDate)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("not-thread-safety-task-executor");
        return executor;
    }

}
