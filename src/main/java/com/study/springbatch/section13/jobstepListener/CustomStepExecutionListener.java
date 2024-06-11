package com.study.springbatch.section13.jobstepListener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class CustomStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        stepExecution.getExecutionContext().put("name", "user1");

        System.out.println("Step " + stepName + " started");

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        ExitStatus exitStatus = stepExecution.getExitStatus();
        BatchStatus status = stepExecution.getStatus();
        System.out.println("status: " + status);
        System.out.println("exitStatus: " + exitStatus);

        String name = (String) stepExecution.getExecutionContext().get("name");
        System.out.println("name = " +  name);

        return ExitStatus.COMPLETED;
    }
}
