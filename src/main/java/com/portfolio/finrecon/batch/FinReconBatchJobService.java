package com.portfolio.finrecon.batch;

import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.portfolio.finrecon.common.exception.DomainException;

@Service
public class FinReconBatchJobService {

    private final JobLauncher jobLauncher;
    private final Job dailyReconciliationJob;
    private final Job dailySettlementJob;

    public FinReconBatchJobService(JobLauncher jobLauncher, Job dailyReconciliationJob, Job dailySettlementJob) {
        this.jobLauncher = jobLauncher;
        this.dailyReconciliationJob = dailyReconciliationJob;
        this.dailySettlementJob = dailySettlementJob;
    }

    public void runDailyReconciliation(LocalDate businessDate) {
        run(dailyReconciliationJob, businessDate);
    }

    public void runDailySettlement(LocalDate businessDate) {
        run(dailySettlementJob, businessDate);
    }

    private void run(Job job, LocalDate businessDate) {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addString(FinReconBatchConfig.BUSINESS_DATE_PARAMETER, businessDate.toString())
                    .addLong("requestedAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(job, parameters);
        } catch (Exception exception) {
            throw new DomainException(HttpStatus.INTERNAL_SERVER_ERROR, "BATCH_EXECUTION_FAILED",
                    "Batch job execution failed.");
        }
    }
}
