package com.bybygo.loyalty.batch;

import com.bybygo.loyalty.model.entity.CustomerRewards;
import com.bybygo.loyalty.repository.CustomerRewardsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@RequiredArgsConstructor
public class CustomerRewardsWriter implements ItemWriter<CustomerRewards> {

  @Autowired private CustomerRewardsRepository customerRewardsRepository;

  @Override
  public void write(Chunk<? extends CustomerRewards> rewards) {
    log.debug("Writing {} customer rewards", rewards.size());
    customerRewardsRepository.saveAll(rewards.getItems());
  }
}
