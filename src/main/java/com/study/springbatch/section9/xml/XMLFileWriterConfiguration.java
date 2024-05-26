package com.study.springbatch.section9.xml;

import com.study.springbatch.entity.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//@Configuration
@RequiredArgsConstructor
public class XMLFileWriterConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;
    private final DataSource dataSource;
    private final int chunkSize = 5;

    @Bean
    public Job job() throws Exception {

        LocalDateTime now = LocalDateTime.now();

        return new JobBuilder("XML Configuration Job - " + now, jobRepository)
                .incrementer(new RunIdIncrementer())
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
    public JdbcPagingItemReader<Person> customItemReader() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", "user%");

        return new JdbcPagingItemReaderBuilder<Person>()
                .name("jdbcPagingItemReader")
                .pageSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Person.class))
                .queryProvider(createQueryProvider())
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {

        SqlPagingQueryProviderFactoryBean sqlPagingQueryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        sqlPagingQueryProviderFactoryBean.setDataSource(dataSource);
        sqlPagingQueryProviderFactoryBean.setSelectClause("id,age,username");
        sqlPagingQueryProviderFactoryBean.setFromClause("from person");
        sqlPagingQueryProviderFactoryBean.setWhereClause("where username like :username");

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        sqlPagingQueryProviderFactoryBean.setSortKeys(sortKeys);

        return sqlPagingQueryProviderFactoryBean.getObject();
    }

    @Bean
    public ItemWriter<? super Person> customItemWriter() {
        return new StaxEventItemWriterBuilder<Person>()
                .name("staxEventWriter")
                .marshaller(itemMarshaller())
                .resource(new FileSystemResource("F:\\IdeaProject\\springbatch\\src\\main\\resources\\person.xml"))
                .rootTagName("persons")
                .build();
    }

    @Bean
    public Marshaller itemMarshaller() {
        Map<String, Class<?>> aliases = new HashMap<>();

        aliases.put("person", Person.class);
        aliases.put("id", Long.class);
        aliases.put("age", Long.class);
        aliases.put("username", String.class);

        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);
        return xStreamMarshaller;
    }
}
