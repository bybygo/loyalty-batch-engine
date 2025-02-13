package com.bybygo.loyalty.model.dto.response;

import com.bybygo.loyalty.model.dto.base.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthCheckResponse extends BaseResponse<HealthCheckResponse.HealthDetails> {

  private String errorMessage;

  @Data
  @SuperBuilder
  @NoArgsConstructor
  public static class HealthDetails {
    private String status;
    private LocalDateTime timestamp;
    private String version;
    private Map<String, ComponentStatus> components;
    private SystemMetrics systemMetrics;
  }

  @Data
  @SuperBuilder
  @NoArgsConstructor
  public static class ComponentStatus {
    private String status;
    private String message;
    private LocalDateTime lastChecked;
    private Map<String, Object> details;
  }

  @Data
  @SuperBuilder
  @NoArgsConstructor
  public static class SystemMetrics {
    private Long totalMemory;
    private Long freeMemory;
    private Long maxMemory;
    private Integer availableProcessors;
    private Double systemLoad;
    private Long uptime;
  }
}
