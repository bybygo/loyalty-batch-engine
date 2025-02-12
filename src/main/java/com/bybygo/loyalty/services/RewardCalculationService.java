package com.bybygo.loyalty.services;

import com.bybygo.loyalty.enums.CustomerTier;
import com.bybygo.loyalty.model.entity.CustomerRewards;
import com.bybygo.loyalty.model.entity.Transaction;
import com.bybygo.loyalty.repository.CustomerRewardsRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardCalculationService {

  private final CustomerRewardsRepository customerRewardsRepository;

  @Transactional
  public CustomerRewards calculateRewards(Transaction transaction) {
    CustomerRewards rewards = getOrCreateCustomerRewards(transaction.getCustomerId());
    updateRewards(rewards, transaction);
    return customerRewardsRepository.save(rewards);
  }

  private CustomerRewards getOrCreateCustomerRewards(Long customerId) {
    return customerRewardsRepository
        .findByCustomerId(customerId)
        .orElseGet(() -> createNewCustomerRewards(customerId));
  }

  private CustomerRewards createNewCustomerRewards(Long customerId) {
    CustomerRewards rewards = new CustomerRewards();
    rewards.setCustomerId(customerId);
    rewards.setLoyaltyPoints(0);
    rewards.setTotalSpent(BigDecimal.ZERO);
    rewards.setTier(CustomerTier.BRONZE.name());
    return rewards;
  }

  private void updateRewards(CustomerRewards rewards, Transaction transaction) {
    // Update points
    int pointsEarned = calculatePoints(transaction.getAmount());
    rewards.setLoyaltyPoints(rewards.getLoyaltyPoints() + pointsEarned);

    // Update total spent
    BigDecimal newTotalSpent = rewards.getTotalSpent().add(transaction.getAmount());
    rewards.setTotalSpent(newTotalSpent);

    // Update tier
    CustomerTier newTier = CustomerTier.fromSpentAmount(newTotalSpent);
    rewards.setTier(newTier.name());

    log.debug(
        "Updated rewards for customer {}: points={}, tier={}",
        rewards.getCustomerId(),
        rewards.getLoyaltyPoints(),
        rewards.getTier());
  }

  private int calculatePoints(BigDecimal amount) {
    return amount.intValue();
  }
}
