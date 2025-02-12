package com.bybygo.loyalty.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Entity
@Table(name = "customer_rewards")
public class CustomerRewards {
  @Id private Long customerId;

  @Column(name = "loyalty_points", nullable = false)
  private Integer loyaltyPoints;

  @Column(nullable = false)
  private String tier;

  @Column(name = "total_spent")
  private BigDecimal totalSpent;

  @Version private Long version;
}
