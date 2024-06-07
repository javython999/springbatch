package com.study.springbatch.section12.parallel;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class CustomTasklet implements Tasklet {

    private long sum;
    private Object lock = new Object();

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        synchronized (lock) {
            for (int i = 1; i <= 100; i++) {
                sum += i;
            }

            System.out.printf("%s has been executed on thread %s%n", chunkContext.getStepContext().getStepName(), Thread.currentThread().getName());
            System.out.println("sum = " + sum);
        }

        return RepeatStatus.FINISHED;
    }
}
