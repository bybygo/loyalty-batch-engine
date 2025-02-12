package com.bybygo.loyalty.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TransactionRequest {
  @NotNull @Positive private Long customerId;

  @NotNull @Positive private BigDecimal amount;

  @NotNull private LocalDateTime transactionDate;

  private String transactionType;
}
