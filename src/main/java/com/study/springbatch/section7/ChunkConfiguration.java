package com.study.springbatch.section7;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

//@Configuration
@RequiredArgsConstructor
public class ChunkConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;


    @Bean
    public Job chunkJob() {
        return new JobBuilder("ChunkJob", jobRepository)
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("ChunkJob-step1", jobRepository)
                .<String, String>chunk(5,  tx)
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5")))
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        Thread.sleep(500);
                        System.out.println("item = " + item);
                        return "my " + item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(Chunk<? extends String> chunk) throws Exception {
                        Thread.sleep(500);
                        System.out.println("chunk = " + chunk);
                    }
                })
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("ChunkJob-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}
