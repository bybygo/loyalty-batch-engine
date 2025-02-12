package com.bybygo.loyalty.services;

import com.bybygo.loyalty.model.dto.JobMetrics;
import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobMetricsService {

  public JobMetrics extractJobMetrics(JobExecution jobExecution) {
    long readCount = calculateTotalReadCount(jobExecution);
    long writeCount = calculateTotalWriteCount(jobExecution);

    LocalDateTime startTime = jobExecution.getStartTime();
    LocalDateTime endTime = jobExecution.getEndTime();

    Double processingTime = calculateProcessingTime(startTime, endTime);
    Double itemsPerSecond = calculateItemsPerSecond(readCount, processingTime);

    JobMetrics metrics =
        JobMetrics.builder()
            .readCount(readCount)
            .writeCount(writeCount)
            .skipCount(calculateTotalSkipCount(jobExecution))
            .commitCount(calculateTotalCommitCount(jobExecution))
            .rollbackCount(calculateTotalRollbackCount(jobExecution))
            .startTime(startTime)
            .endTime(endTime)
            .status(jobExecution.getStatus())
            .exitCode(jobExecution.getExitStatus().getExitCode())
            .processingTime(processingTime)
            .itemsPerSecond(itemsPerSecond)
            .build();

    log.debug("Extracted metrics for job {}: {}", jobExecution.getJobId(), metrics);
    return metrics;
  }

  public String extractErrorMessage(JobExecution jobExecution) {
    return jobExecution.getAllFailureExceptions().stream()
        .map(Throwable::getMessage)
        .distinct()
        .collect(Collectors.joining("; "));
  }

  private Double calculateProcessingTime(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime != null && endTime != null) {
      return (double) java.time.Duration.between(startTime, endTime).toMillis() / 1000.0;
    }
    return null;
  }

  private Double calculateItemsPerSecond(long itemCount, Double processingTime) {
    if (processingTime != null && processingTime > 0) {
      return itemCount / processingTime;
    }
    return null;
  }

  private long calculateTotalReadCount(JobExecution jobExecution) {
    return sumMetric(jobExecution, StepExecution::getReadCount);
  }

  private long calculateTotalWriteCount(JobExecution jobExecution) {
    return sumMetric(jobExecution, StepExecution::getWriteCount);
  }

  private long calculateTotalSkipCount(JobExecution jobExecution) {
    return sumMetric(jobExecution, StepExecution::getSkipCount);
  }

  private long calculateTotalCommitCount(JobExecution jobExecution) {
    return sumMetric(jobExecution, StepExecution::getCommitCount);
  }

  private long calculateTotalRollbackCount(JobExecution jobExecution) {
    return sumMetric(jobExecution, StepExecution::getRollbackCount);
  }

  private long sumMetric(JobExecution jobExecution, Function<StepExecution, Long> metricExtractor) {
    return jobExecution.getStepExecutions().stream().mapToLong(metricExtractor::apply).sum();
  }
}
