package com.bybygo.loyalty.enums;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerTier {
  BRONZE(BigDecimal.ZERO),
  SILVER(new BigDecimal("1000")),
  GOLD(new BigDecimal("5000")),
  PLATINUM(new BigDecimal("10000"));

  private final BigDecimal minimumSpend;

  public static CustomerTier fromSpentAmount(BigDecimal amount) {
    CustomerTier highestTier = BRONZE;
    for (CustomerTier tier : values()) {
      if (amount.compareTo(tier.getMinimumSpend()) > 0) {
        highestTier = tier;
      }
    }
    return highestTier;
  }
}
