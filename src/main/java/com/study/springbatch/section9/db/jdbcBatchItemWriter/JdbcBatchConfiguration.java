package com.study.springbatch.section9.db.jdbcBatchItemWriter;

import com.study.springbatch.entity.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//@Configuration
@RequiredArgsConstructor
public class JdbcBatchConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final DataSource dataSource;
    private final int chunkSize = 10;

    @Bean
    public Job job() {
        LocalDateTime now = LocalDateTime.now();
        return new JobBuilder("JdbcBatchJob - " + now, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<Person, Person>chunk(chunkSize, tx)
                .reader(customItemReader())
                .writer(customItemWriter())
                .build();
    }


    @Bean
    public ItemWriter<? super Person> customItemWriter() {
        return new JdbcBatchItemWriterBuilder<Person>()
                .dataSource(dataSource)
                .sql("insert into person2 values(:id, :age, :username)")
                .beanMapped()
                .build();
    }


    @Bean
    public ItemReader<? extends Person> customItemReader() {
        JdbcPagingItemReader<Person> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(dataSource);
        reader.setFetchSize(chunkSize);
        reader.setRowMapper(new RowMapper<Person>() {
            @Override
            public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
                Person person = new Person();
                person.setId(rs.getInt("id"));
                person.setAge(rs.getInt("age"));
                person.setUsername(rs.getString("username"));
                return person;
            }
        });

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("id", Order.ASCENDING);

        MySqlPagingQueryProvider mySqlPagingQueryProvider = new MySqlPagingQueryProvider();
        mySqlPagingQueryProvider.setSelectClause("id, age, username");
        mySqlPagingQueryProvider.setFromClause("from person");
        mySqlPagingQueryProvider.setWhereClause("where username like :username");
        mySqlPagingQueryProvider.setSortKeys(sortKey);


        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("username", "user%");

        reader.setQueryProvider(mySqlPagingQueryProvider);
        reader.setParameterValues(parameters);

        return reader;
    }
}
