package com.portfolio.finrecon.batch;

import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.portfolio.finrecon.service.ReconciliationService;
import com.portfolio.finrecon.service.SettlementService;

@Configuration
public class FinReconBatchConfig {

    public static final String BUSINESS_DATE_PARAMETER = "businessDate";

    @Bean
    public Job dailyReconciliationJob(JobRepository jobRepository, Step dailyReconciliationStep) {
        return new JobBuilder("dailyReconciliationJob", jobRepository)
                .start(dailyReconciliationStep)
                .build();
    }

    @Bean
    public Step dailyReconciliationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            ReconciliationService reconciliationService) {
        return new StepBuilder("dailyReconciliationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LocalDate businessDate = businessDate(contribution.getStepExecution().getJobParameters()
                            .getString(BUSINESS_DATE_PARAMETER));
                    reconciliationService.run(businessDate);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job dailySettlementJob(JobRepository jobRepository, Step dailySettlementStep) {
        return new JobBuilder("dailySettlementJob", jobRepository)
                .start(dailySettlementStep)
                .build();
    }

    @Bean
    public Step dailySettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            SettlementService settlementService) {
        return new StepBuilder("dailySettlementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LocalDate businessDate = businessDate(contribution.getStepExecution().getJobParameters()
                            .getString(BUSINESS_DATE_PARAMETER));
                    settlementService.runDaily(businessDate);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private LocalDate businessDate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("businessDate job parameter is required.");
        }
        return LocalDate.parse(value);
    }
}
