package com.bybygo.loyalty.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
  private Long id;
  private Long customerId;
  private BigDecimal amount;
  private LocalDateTime transactionDate;
  private String transactionType;
  private boolean processed;
}
