package com.bybygo.loyalty.controller;

import com.bybygo.loyalty.model.dto.response.HealthCheckResponse;
import com.bybygo.loyalty.model.dto.response.HealthCheckResponse.ComponentStatus;
import com.bybygo.loyalty.model.dto.response.HealthCheckResponse.HealthDetails;
import com.bybygo.loyalty.model.dto.response.HealthCheckResponse.SystemMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "APIs for system health monitoring")
public class HealthCheckController {

  @GetMapping
  @Operation(
      summary = "Check service health",
      description = "Retrieves the current health status of the service and its components")
  public ResponseEntity<HealthCheckResponse> checkHealth() {
    log.debug("Performing health check");

    HealthDetails healthDetails =
        HealthDetails.builder()
            .status("UP")
            .timestamp(LocalDateTime.now())
            .version("0.0.1")
            .components(getComponentStatuses())
            .systemMetrics(getSystemMetrics())
            .build();

    return ResponseEntity.ok(
        HealthCheckResponse.builder()
            .success(true)
            .message("Health check completed successfully")
            .data(healthDetails)
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .build());
  }

  private Map<String, ComponentStatus> getComponentStatuses() {
    Map<String, ComponentStatus> components = new HashMap<>();

    // Database status example
    components.put(
        "database",
        ComponentStatus.builder()
            .status("UP")
            .message("Database connection is healthy")
            .lastChecked(LocalDateTime.now())
            .build());

    // Batch status example
    components.put(
        "batch",
        ComponentStatus.builder()
            .status("UP")
            .message("Batch service is responding")
            .lastChecked(LocalDateTime.now())
            .build());

    return components;
  }

  private SystemMetrics getSystemMetrics() {
    Runtime runtime = Runtime.getRuntime();
    com.sun.management.OperatingSystemMXBean os =
        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    return SystemMetrics.builder()
        .totalMemory(runtime.totalMemory())
        .freeMemory(runtime.freeMemory())
        .maxMemory(runtime.maxMemory())
        .availableProcessors(runtime.availableProcessors())
        .systemLoad(os.getSystemLoadAverage())
        .uptime(ManagementFactory.getRuntimeMXBean().getUptime())
        .build();
  }
}
