package com.bybygo.loyalty.model.dto.base;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class BaseRequest {
  @NotNull private String requestId;
  private String description;
}
