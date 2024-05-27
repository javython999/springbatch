package com.study.springbatch.section8.xml;

import com.study.springbatch.entity.Customer;
import com.thoughtworks.xstream.XStream;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

//@Configuration
@RequiredArgsConstructor
public class XMLConfiguration {
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
        return new StaxEventItemReaderBuilder<Customer>()
                .name("staxXml")
                .resource(new ClassPathResource("customer.xml"))
                .addFragmentRootElements("customer")
                .unmarshaller(itemUnmarshaller())
                .build();
    }

    @Bean
    public Unmarshaller itemUnmarshaller() {

        Map<String , Class<?>> aliases = new HashMap<>();
        aliases.put("customer", Customer.class);
        aliases.put("name", String.class);
        aliases.put("age", Integer.class);
        aliases.put("year", Integer.class);

        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);

        XStream xstream = xStreamMarshaller.getXStream();
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypes(new Class[]{Customer.class});

        return xStreamMarshaller;
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
