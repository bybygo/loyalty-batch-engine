package com.bybygo.loyalty.model.dto.request;

import com.bybygo.loyalty.model.dto.base.BaseRequest;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BatchJobRequest extends BaseRequest {
  @NotNull private LocalDate processDate;
}
