package com.balu.bankflow.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {

    // Inject: JobLauncher, Job emiDeductionJob
    //   (Job needs @Qualifier("emiDeductionJob") since Spring Batch has multiple Job beans possible)
    private final JobLauncher jobLauncher;

    @Qualifier("emiDeductionJob")
    private final Job emiDeductionJob;

    // METHOD: runEmiDeductionJob()
    // Annotation: @Scheduled(cron = "0 0 1 1 * ?")  // 1 AM on the 1st of every month
    // WHAT to return: void
    @Scheduled(cron = "0 0 1 1 * ?")
    public void runEmiDeductionJob() throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {

        try {
            // WHAT to do:
            //   Step 1: Build JobParameters with a unique value (e.g. current timestamp)
            //           — required because Spring Batch won't rerun a job with identical parameters
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            //   Step 2: jobLauncher.run(emiDeductionJob, jobParameters)
            jobLauncher.run(emiDeductionJob, jobParameters);

            //   Step 3: log.info "EMI deduction job triggered"
            log.info("EMI deduction job triggered");
        } catch (Exception e) {
            //   Step 4: catch and log any exception (don't let scheduler thread die silently)
            log.error("EMI deduction job exception", e);
        }

    }
}
