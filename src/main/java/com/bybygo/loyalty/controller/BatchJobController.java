package com.bybygo.loyalty.controller;

import com.bybygo.loyalty.model.dto.base.BaseResponse;
import com.bybygo.loyalty.model.dto.request.BatchJobRequest;
import com.bybygo.loyalty.services.BatchJobService;
import com.bybygo.loyalty.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@Tag(name = "Batch Operations", description = "APIs for batch job operations")
public class BatchJobController {

  private final BatchJobService batchJobService;

  @PostMapping("/jobs")
  @Operation(
      summary = "Start a new batch job",
      description = "Initiates a new batch processing job with the given parameters")
  public ResponseEntity<BaseResponse<String>> startJob(
      @Valid @RequestBody BatchJobRequest request) {
    log.debug("Received batch job request: {}", request);
    String jobId = batchJobService.startJob(request);
    return ResponseEntity.ok(ResponseUtil.success("Job started successfully", jobId));
  }
}
