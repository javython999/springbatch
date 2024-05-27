package com.study.springbatch.section6;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class JobScopeStepScopeConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public Job JobScopeStepScopeJob() {
        return new JobBuilder("JobScopeStepScopeJob", jobRepository)
                .start(step1(null))
                .next(step2())
                .listener(new JobListener())
                .build();
    }

    @Bean
    @JobScope
    public Step step1(@Value("#{jobParameters['message']}") String message) {

        System.out.println("message = " + message);

        return new StepBuilder("JobScopeStepScopeJob-step1", jobRepository)
                .tasklet(tasklet1(null), tx)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("JobScopeStepScopeJob-step2", jobRepository)
                .tasklet(tasklet2(null), tx)
                .listener(new CustomStepListener())
                .build();
    }

    @Bean
    @StepScope
    public Tasklet tasklet1(@Value("#{jobExecutionContext['name']}") String name) {
        System.out.println("name = " + name);
        return (contribution, chunkContext) -> {
            System.out.println("tasklet was executed");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @StepScope
    public Tasklet tasklet2(@Value("#{stepExecutionContext['name2']}") String name2) {
        System.out.println("name2 = " + name2);
        return (contribution, chunkContext) -> {
            System.out.println("tasklet was executed");
            return RepeatStatus.FINISHED;
        };
    }



}
