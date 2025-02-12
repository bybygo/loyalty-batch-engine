// services/BatchJobService.java
package com.bybygo.loyalty.services;

import com.bybygo.loyalty.constants.BatchConstants;
import com.bybygo.loyalty.exceptions.BatchJobException;
import com.bybygo.loyalty.model.dto.request.BatchJobRequest;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

  private final JobLauncher jobLauncher;
  private final Job processTransactionsJob;
  private final JobParametersValidator jobParametersValidator;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

  @Retryable(
      retryFor = {Exception.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000))
  public String startJob(BatchJobRequest request) {
    String jobId = UUID.randomUUID().toString();
    JobParameters parameters = createJobParameters(jobId, request);

    try {
      jobParametersValidator.validate(parameters);
      JobExecution jobExecution = jobLauncher.run(processTransactionsJob, parameters);
      logJobStatus(jobId, jobExecution);
      return jobId;
    } catch (Exception e) {
      throw handleJobException(e, jobId);
    }
  }

  private JobParameters createJobParameters(String jobId, BatchJobRequest request) {
    return new JobParametersBuilder()
        .addString(BatchConstants.JOB_PARAM_ID, jobId)
        .addString(BatchConstants.JOB_PARAM_DATE, request.getProcessDate().format(DATE_FORMATTER))
        .addString(BatchConstants.JOB_PARAM_DESC, request.getDescription())
        .toJobParameters();
  }

  private void logJobStatus(String jobId, JobExecution jobExecution) {
    log.info(
        "Job started - ID: {}, Status: {}, Start Time: {}",
        jobId,
        jobExecution.getStatus(),
        jobExecution.getStartTime());
  }

  private BatchJobException handleJobException(Exception e, String jobId) {
    log.error("Failed to start job: {}", jobId, e);
    return new BatchJobException("Failed to start batch job: " + jobId, e);
  }
}
