package com.bybygo.loyalty.repository;

import com.bybygo.loyalty.model.entity.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  Page<Transaction> findByProcessedFalse(Pageable pageable);

  List<Transaction> findByProcessedFalse();

  @Query(
      "SELECT t FROM Transaction t WHERE t.processed = false AND t.transactionDate BETWEEN :startDate AND :endDate")
  Page<Transaction> findUnprocessedTransactionsInDateRange(
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  @Query("SELECT COUNT(t) FROM Transaction t WHERE t.processed = false")
  long countUnprocessedTransactions();

  @Query("SELECT t FROM Transaction t WHERE t.processed = false AND t.customerId = :customerId")
  Page<Transaction> findUnprocessedTransactionsByCustomer(
      @Param("customerId") Long customerId, Pageable pageable);
}
