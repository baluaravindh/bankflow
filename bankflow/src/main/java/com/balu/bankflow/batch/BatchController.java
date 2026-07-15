package com.balu.bankflow.batch;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
@Tag(name = "Batch", description = "Batch Job Trigger APIs")
public class BatchController {

    // Inject: JobLauncher, Job emiDeductionJob
    private final JobLauncher jobLauncher;
    private final Job emiDeductionJob;

    // METHOD: triggerEmiDeduction()
    // WHO: ADMIN only (@PreAuthorize("hasAuthority('BANK_ADMIN')"))
    // Endpoint: POST /api/admin/batch/emi-deduction
    // WHAT to do:
    //   Step 1: Build JobParameters with unique timestamp value (same as scheduler)
    //   Step 2: jobLauncher.run(emiDeductionJob, jobParameters)
    //   Step 3: Return success message
    // WHAT to return: ResponseEntity<String> — e.g. "EMI deduction job triggered successfully"

    @Operation(summary = "Trigger Emi Deduction")
    @PostMapping("/emi-deduction")
    @PreAuthorize("hasRole('BANK_ADMIN')")
    public ResponseEntity<String> triggerEmiDeduction() throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(emiDeductionJob, jobParameters);
        return ResponseEntity.ok("EMI deduction job triggered successfully");
    }
}
