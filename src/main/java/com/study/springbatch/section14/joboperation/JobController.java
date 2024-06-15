package com.study.springbatch.section14.joboperation;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.*;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.Properties;

@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobRegistry jobRegistry;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;


    @PostMapping("/batch/start")
    public String start(@RequestBody JobInfo jobInfo) throws NoSuchJobException, JobInstanceAlreadyExistsException, JobParametersInvalidException {
        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext(); ) {
            Job job = jobRegistry.getJob(iterator.next());

            System.out.println("start - jobName : " + job.getName());

            Properties properties = new Properties();
            properties.put("id", jobInfo.getId());

            jobOperator.start(job.getName(), properties);
        }


        return "batch is started";
    }

    @PostMapping("/batch/stop")
    public String stop() throws NoSuchJobException, NoSuchJobExecutionException, JobExecutionNotRunningException {
        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext(); ) {
            Job job = jobRegistry.getJob(iterator.next());

            System.out.println("stop - jobName : " + job.getName());


            for (Iterator<JobExecution> jobexcution = jobExplorer.findRunningJobExecutions(job.getName()).iterator(); iterator.hasNext(); ) {
                jobOperator.stop(jobexcution.next().getJobId());
            }
        }
        return "batch will be stop";
    }

    @PostMapping("/batch/restart")
    public String restart() throws NoSuchJobException, NoSuchJobExecutionException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, JobRestartException {
        for (Iterator<String> iterator = jobRegistry.getJobNames().iterator(); iterator.hasNext(); ) {
            Job job = jobRegistry.getJob(iterator.next());

            System.out.println("restart - jobName : " + job.getName());

            JobInstance lastJobInstance = jobExplorer.getLastJobInstance(job.getName());

            jobOperator.restart(lastJobInstance.getInstanceId());
        }
        return "batch will be stop";
    }
}
