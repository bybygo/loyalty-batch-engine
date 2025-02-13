package com.bybygo.loyalty.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bybygo.loyalty.model.dto.JobMetrics;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

class JobMetricsServiceTest {

  private JobMetricsService jobMetricsService;
  private JobExecution jobExecution;

  @BeforeEach
  void setUp() {
    jobMetricsService = new JobMetricsService();
    jobExecution = mock(JobExecution.class);
  }

  @Test
  void extractJobMetrics_WithValidExecution_ShouldReturnCorrectMetrics() {
    // Arrange
    LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
    LocalDateTime endTime = LocalDateTime.now();

    StepExecution step1 = createStepExecution(10L, 8L, 1L, 2L, 0L);
    StepExecution step2 = createStepExecution(15L, 12L, 2L, 3L, 1L);

    when(jobExecution.getStepExecutions()).thenReturn(Arrays.asList(step1, step2));
    when(jobExecution.getStartTime()).thenReturn(startTime);
    when(jobExecution.getEndTime()).thenReturn(endTime);
    when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
    when(jobExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);

    // Act
    JobMetrics metrics = jobMetricsService.extractJobMetrics(jobExecution);

    // Assert
    assertThat(metrics).isNotNull();
    assertThat(metrics.getReadCount()).isEqualTo(25L); // 10 + 15
    assertThat(metrics.getWriteCount()).isEqualTo(20L); // 8 + 12
    assertThat(metrics.getSkipCount()).isEqualTo(3L); // 1 + 2
    assertThat(metrics.getCommitCount()).isEqualTo(5L); // 2 + 3
    assertThat(metrics.getRollbackCount()).isEqualTo(1L); // 0 + 1
    assertThat(metrics.getStartTime()).isEqualTo(startTime);
    assertThat(metrics.getEndTime()).isEqualTo(endTime);
    assertThat(metrics.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertThat(metrics.getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
    assertThat(metrics.getProcessingTime()).isPositive();
    assertThat(metrics.getItemsPerSecond()).isPositive();
  }

  @Test
  void extractJobMetrics_WithNoSteps_ShouldReturnZeroMetrics() {
    // Arrange
    when(jobExecution.getStepExecutions()).thenReturn(Collections.emptyList());
    when(jobExecution.getStartTime()).thenReturn(null);
    when(jobExecution.getEndTime()).thenReturn(null);
    when(jobExecution.getStatus()).thenReturn(BatchStatus.STARTED);
    when(jobExecution.getExitStatus()).thenReturn(ExitStatus.UNKNOWN);

    // Act
    JobMetrics metrics = jobMetricsService.extractJobMetrics(jobExecution);

    // Assert
    assertThat(metrics).isNotNull();
    assertThat(metrics.getReadCount()).isZero();
    assertThat(metrics.getWriteCount()).isZero();
    assertThat(metrics.getSkipCount()).isZero();
    assertThat(metrics.getCommitCount()).isZero();
    assertThat(metrics.getRollbackCount()).isZero();
    assertThat(metrics.getProcessingTime()).isNull();
    assertThat(metrics.getItemsPerSecond()).isNull();
  }

  @Test
  void extractErrorMessage_WithFailureExceptions_ShouldReturnConcatenatedMessages() {
    // Arrange
    ArrayList<Throwable> exceptions = new ArrayList<>();
    exceptions.add(new RuntimeException("Error 1"));
    exceptions.add(new RuntimeException("Error 2"));
    exceptions.add(new RuntimeException("Error 1")); // Duplicate error

    when(jobExecution.getAllFailureExceptions()).thenReturn(exceptions);

    // Act
    String errorMessage = jobMetricsService.extractErrorMessage(jobExecution);

    // Assert
    assertThat(errorMessage).isEqualTo("Error 1; Error 2");
  }

  @Test
  void extractErrorMessage_WithNoExceptions_ShouldReturnEmptyString() {
    // Arrange
    when(jobExecution.getAllFailureExceptions()).thenReturn(Collections.emptyList());

    // Act
    String errorMessage = jobMetricsService.extractErrorMessage(jobExecution);

    // Assert
    assertThat(errorMessage).isEmpty();
  }

  private StepExecution createStepExecution(
      Long readCount, Long writeCount, Long skipCount, Long commitCount, Long rollbackCount) {
    StepExecution stepExecution = mock(StepExecution.class);
    when(stepExecution.getReadCount()).thenReturn(readCount);
    when(stepExecution.getWriteCount()).thenReturn(writeCount);
    when(stepExecution.getSkipCount()).thenReturn(skipCount);
    when(stepExecution.getCommitCount()).thenReturn(commitCount);
    when(stepExecution.getRollbackCount()).thenReturn(rollbackCount);
    return stepExecution;
  }
}
