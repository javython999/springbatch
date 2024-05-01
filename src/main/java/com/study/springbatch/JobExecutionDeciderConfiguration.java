package com.study.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class JobExecutionDeciderConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job jobExecutionDeciderJob() {
        return new JobBuilder("JobExecutionDecider-Job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step())
                .next(decider())
                .from(decider()).on("ODD").to(oddStep())
                .from(decider()).on("EVEN").to(evenStep())
                .end()
                .build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new CustomJobExecutionDecider();
    }

    @Bean
    public Step step() {
        return new StepBuilder("JobExecutionDecider-step", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("JobExecutionDecider-step was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step oddStep() {
        return new StepBuilder("JobExecutionDecider-oddStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("JobExecutionDecider-oddStep was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Step evenStep() {
        return new StepBuilder("JobExecutionDecider-evenStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("JobExecutionDecider-evenStep was executed");
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }
}
