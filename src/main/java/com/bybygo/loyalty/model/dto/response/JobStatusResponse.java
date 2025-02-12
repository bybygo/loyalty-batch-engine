package com.bybygo.loyalty.model.dto.response;

import com.bybygo.loyalty.model.dto.base.BaseResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobStatusResponse extends BaseResponse<JobStatusResponse.JobDetails> {

  private String errorMessage;

  @Data
  @SuperBuilder
  @NoArgsConstructor
  public static class JobDetails {
    private String jobId;
    private String jobName;
    private BatchStatus status;
    private ExitStatus exitStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private Map<String, Object> parameters;
    private Object metrics;
  }
}
