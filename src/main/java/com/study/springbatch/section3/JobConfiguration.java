package com.study.springbatch.section3;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    //@Bean
    public Job batchJob1() {
        return new JobBuilder("batchJob1", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Job batchJob2() {
        return new JobBuilder("batchJob2", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(flow())
                .next(step5())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("JobConfiguration-step1", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step1 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("JobConfiguration-step2", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step2 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Flow flow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
        flowBuilder.start(step3())
                .next(step4())
                .end();
        return flowBuilder.build();
    }

    @Bean
    public Step step3() {
        return new StepBuilder("JobConfiguration-step3", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step3 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step4() {
        return new StepBuilder("JobConfiguration-step4", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step4 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }

    @Bean
    public Step step5() {
        return new StepBuilder("JobConfiguration-step5", jobRepository)
                .tasklet(((contribution, chunkConext) -> {
                    System.out.println("JobConfiguration-step5 was executed");
                    return RepeatStatus.FINISHED;
                }), tx).build();
    }
}
