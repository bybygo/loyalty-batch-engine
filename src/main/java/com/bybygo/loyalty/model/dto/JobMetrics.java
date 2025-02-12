package com.bybygo.loyalty.model.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.batch.core.BatchStatus;

@Data
@Builder
public class JobMetrics {
  private long readCount;
  private long writeCount;
  private long skipCount;
  private long commitCount;
  private long rollbackCount;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private BatchStatus status;
  private String exitCode;
  private Double processingTime;
  private Double itemsPerSecond;
}
