package com.bybygo.loyalty.repository;

import com.bybygo.loyalty.model.entity.CustomerRewards;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface CustomerRewardsRepository extends JpaRepository<CustomerRewards, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
  Optional<CustomerRewards> findByCustomerId(Long customerId);
}
