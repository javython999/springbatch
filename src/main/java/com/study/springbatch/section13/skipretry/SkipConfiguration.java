package com.study.springbatch.section13.skipretry;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

//@Configuration
@RequiredArgsConstructor
public class SkipConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job job() {
        return new JobBuilder("skipJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1", jobRepository)
                .<Integer, String>chunk(10, tx)
                .reader(listItemReader())
                .processor(new ItemProcessor<Integer, String>() {
                    @Override
                    public String process(Integer item) throws Exception {
                        if (item == 4) {
                            throw new CustomSkipException("process skipped");
                        }
                        System.out.println("process : item" + item);
                        return "item"+ item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(Chunk<? extends String> chunk) throws Exception {
                        for (String item : chunk.getItems()) {
                            if (item.equals("item5")) {
                                throw new CustomSkipException("write skipped");
                            }
                            System.out.println("write : " + item);
                        }
                    }
                })
                .faultTolerant()
                .skip(CustomSkipException.class)
                .skipLimit(5)
                .listener(new CustomSkipListener())
                .build();
    }

    @Bean
    public ItemReader<Integer> listItemReader() {
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        return new LinkedListItemReader<>(list);
    }


}
