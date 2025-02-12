package com.bybygo.loyalty.batch;

import com.bybygo.loyalty.model.entity.CustomerRewards;
import com.bybygo.loyalty.model.entity.Transaction;
import com.bybygo.loyalty.services.RewardCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionProcessor implements ItemProcessor<Transaction, CustomerRewards> {

  private final RewardCalculationService rewardCalculationService;

  @Override
  public CustomerRewards process(Transaction transaction) {
    try {
      log.debug("Processing transaction: {}", transaction.getId());
      CustomerRewards rewards = rewardCalculationService.calculateRewards(transaction);
      transaction.setProcessed(true);
      return rewards;
    } catch (Exception e) {
      log.error("Error processing transaction {}", transaction.getId(), e);
      return null;
    }
  }
}
