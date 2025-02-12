package com.bybygo.loyalty.controller;

import com.bybygo.loyalty.model.dto.base.BaseResponse;
import com.bybygo.loyalty.model.dto.response.JobStatusResponse;
import com.bybygo.loyalty.services.JobStatusService;
import com.bybygo.loyalty.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/batch/status")
@RequiredArgsConstructor
@Tag(name = "Job Status", description = "APIs for monitoring batch job status")
public class JobStatusController {

  private final JobStatusService jobStatusService;

  @GetMapping("/{jobId}")
  @Operation(
      summary = "Get job status",
      description = "Retrieves the current status of a specific batch job")
  public ResponseEntity<BaseResponse<JobStatusResponse.JobDetails>> getJobStatus(
      @Parameter(description = "Unique identifier of the job") @PathVariable String jobId) {
    log.debug("Fetching status for job: {}", jobId);
    JobStatusResponse response = jobStatusService.getJobStatus(jobId);
    return ResponseEntity.ok(
        ResponseUtil.success("Job status retrieved successfully", response.getData()));
  }

  @GetMapping
  @Operation(
      summary = "Get recent jobs",
      description = "Retrieves a list of recent batch jobs with their status")
  public ResponseEntity<BaseResponse<List<JobStatusResponse.JobDetails>>> getRecentJobs(
      @Parameter(description = "Maximum number of jobs to return")
          @RequestParam(defaultValue = "10")
          int limit) {
    log.debug("Fetching recent jobs with limit: {}", limit);
    List<JobStatusResponse> responses = jobStatusService.getRecentJobs(limit);
    List<JobStatusResponse.JobDetails> jobDetails =
        responses.stream().map(JobStatusResponse::getData).toList();
    return ResponseEntity.ok(
        ResponseUtil.success("Recent jobs retrieved successfully", jobDetails));
  }

  @DeleteMapping("/{jobId}")
  @Operation(summary = "Stop job", description = "Attempts to stop a running batch job")
  public ResponseEntity<BaseResponse<JobStatusResponse.JobDetails>> stopJob(
      @Parameter(description = "Unique identifier of the job to stop") @PathVariable String jobId) {
    log.debug("Attempting to stop job: {}", jobId);
    JobStatusResponse response = jobStatusService.stopJob(jobId);
    return ResponseEntity.ok(ResponseUtil.success("Job stopped successfully", response.getData()));
  }
}
