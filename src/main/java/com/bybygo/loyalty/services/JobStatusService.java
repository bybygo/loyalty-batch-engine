package com.bybygo.loyalty.services;

import com.bybygo.loyalty.constants.BatchConstants;
import com.bybygo.loyalty.exceptions.BatchJobException;
import com.bybygo.loyalty.model.dto.JobMetrics;
import com.bybygo.loyalty.model.dto.response.JobStatusResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobStatusService {

  private final JobExplorer jobExplorer;
  private final JobOperator jobOperator;
  private final JobMetricsService jobMetricsService;

  @Value("${batch.job.search.limit:1000}")
  private int jobSearchLimit;

  @Cacheable(
      value = "jobStatus",
      key = "#jobId",
      unless = "#result.data.status == T(org.springframework.batch.core.BatchStatus).STARTED")
  public JobStatusResponse getJobStatus(String jobId) {
    return findJobExecution(jobId)
        .map(this::createJobStatusResponse)
        .orElseThrow(() -> new BatchJobException("Job not found: " + jobId));
  }

  public List<JobStatusResponse> getRecentJobs(int limit) {
    return jobExplorer.getJobNames().stream()
        .flatMap(jobName -> jobExplorer.getJobInstances(jobName, 0, limit).stream())
        .map(jobExplorer::getJobExecutions)
        .flatMap(List::stream)
        .sorted(this::compareJobExecutions)
        .limit(limit)
        .map(this::createJobStatusResponse)
        .toList();
  }

  public JobStatusResponse stopJob(String jobId) {
    try {
      return findJobExecution(jobId)
          .map(this::stopJobExecution)
          .map(this::createJobStatusResponse)
          .orElseThrow(() -> new BatchJobException("Job not found: " + jobId));
    } catch (Exception e) {
      log.error("Failed to stop job: {}", jobId, e);
      throw new BatchJobException("Failed to stop job: " + jobId, e);
    }
  }

  private Optional<JobExecution> findJobExecution(String jobId) {
    return jobExplorer.getJobNames().stream()
        .flatMap(jobName -> jobExplorer.getJobInstances(jobName, 0, jobSearchLimit).stream())
        .flatMap(jobInstance -> jobExplorer.getJobExecutions(jobInstance).stream())
        .filter(
            execution -> {
              String executionJobId =
                  execution.getJobParameters().getString(BatchConstants.JOB_PARAM_ID);
              return jobId.equals(executionJobId);
            })
        .findFirst();
  }

  private JobExecution stopJobExecution(JobExecution jobExecution) {
    try {
      jobOperator.stop(jobExecution.getId());
      return jobExplorer.getJobExecution(jobExecution.getId());
    } catch (Exception e) {
      log.error("Failed to stop job execution: {}", jobExecution.getId(), e);
      throw new BatchJobException("Failed to stop job execution", e);
    }
  }

  private int compareJobExecutions(JobExecution e1, JobExecution e2) {
    LocalDateTime time1 = e1.getStartTime();
    LocalDateTime time2 = e2.getStartTime();

    if (time1 == null && time2 == null) return 0;
    if (time1 == null) return 1;
    if (time2 == null) return -1;

    return time2.compareTo(time1);
  }

  private JobStatusResponse createJobStatusResponse(JobExecution jobExecution) {
    JobInstance jobInstance = jobExecution.getJobInstance();
    JobMetrics metrics = jobMetricsService.extractJobMetrics(jobExecution);
    String errorMessage = jobMetricsService.extractErrorMessage(jobExecution);

    JobStatusResponse.JobDetails details =
        JobStatusResponse.JobDetails.builder()
            .jobId(jobExecution.getJobParameters().getString(BatchConstants.JOB_PARAM_ID))
            .jobName(jobInstance.getJobName())
            .status(jobExecution.getStatus())
            .exitStatus(jobExecution.getExitStatus())
            .startTime(jobExecution.getStartTime())
            .endTime(jobExecution.getEndTime())
            .parameters(
                Map.of(jobInstance.getJobName(), jobExecution.getJobParameters().getParameters()))
            .metrics(metrics)
            .build();

    return JobStatusResponse.builder()
        .success(isJobSuccessful(jobExecution.getStatus()))
        .message(getStatusMessage(jobExecution.getStatus()))
        .data(details)
        .errorMessage(errorMessage)
        .timestamp(LocalDateTime.now().toString())
        .build();
  }

  private boolean isJobSuccessful(BatchStatus status) {
    return status == BatchStatus.COMPLETED;
  }

  private String getStatusMessage(BatchStatus status) {
    return switch (status) {
      case COMPLETED -> "Job completed successfully";
      case STARTED -> "Job is in progress";
      case FAILED -> "Job failed";
      case STOPPED -> "Job was stopped";
      case ABANDONED -> "Job was abandoned";
      default -> "Job status: " + status;
    };
  }
}
