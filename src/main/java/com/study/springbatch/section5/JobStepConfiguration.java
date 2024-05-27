package com.study.springbatch.section5;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
@RequiredArgsConstructor
public class JobStepConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobLauncher jobLauncher;

    @Bean
    public Job parentJob() {
        return new JobBuilder("JobStepConfiguration-parentJob", jobRepository)
                .start(jobStep())
                .next(step2())
                .build();
    }

    @Bean
    public Step jobStep() {
        return new StepBuilder("JobStepConfiguration-jobStep", jobRepository)
                .job(childJob())
                .launcher(jobLauncher)
                .parametersExtractor(jobParametersExtractor())
                .build();
    }

    private DefaultJobParametersExtractor jobParametersExtractor() {
        DefaultJobParametersExtractor extractor = new DefaultJobParametersExtractor();
        extractor.setKeys(new String[]{"name"});
        return extractor;
    }

    @Bean
    public Job childJob() {
        return new JobBuilder("JobStepConfiguration-childJob", jobRepository)
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("JobStepConfiguration-step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("JobStep-step1 was executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step step2() {
        return new StepBuilder("JobStepConfiguration-step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("JobStep-step2 was executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
