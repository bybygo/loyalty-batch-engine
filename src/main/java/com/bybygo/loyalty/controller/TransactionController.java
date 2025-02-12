package com.bybygo.loyalty.controller;

import com.bybygo.loyalty.model.dto.base.BaseResponse;
import com.bybygo.loyalty.model.dto.request.TransactionRequest;
import com.bybygo.loyalty.model.dto.response.TransactionResponse;
import com.bybygo.loyalty.model.entity.Transaction;
import com.bybygo.loyalty.repository.TransactionRepository;
import com.bybygo.loyalty.util.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Endpoints for testing transaction data")
public class TransactionController {

  private final TransactionRepository transactionRepository;

  @PostMapping
  @Operation(summary = "Create test transaction")
  public ResponseEntity<BaseResponse<TransactionResponse>> createTransaction(
      @Valid @RequestBody TransactionRequest request) {
    log.debug("Creating test transaction: {}", request);

    Transaction transaction =
        Transaction.builder()
            .customerId(request.getCustomerId())
            .amount(request.getAmount())
            .transactionDate(request.getTransactionDate())
            .transactionType(request.getTransactionType())
            .processed(false)
            .build();

    Transaction savedTransaction = transactionRepository.save(transaction);
    return ResponseEntity.ok(
        ResponseUtil.success("Transaction created successfully", mapToResponse(savedTransaction)));
  }

  @GetMapping
  @Operation(summary = "Get all transactions")
  public ResponseEntity<BaseResponse<List<TransactionResponse>>> getAllTransactions(
      @RequestParam(defaultValue = "false") boolean onlyUnprocessed) {
    List<Transaction> transactions =
        onlyUnprocessed
            ? transactionRepository.findByProcessedFalse()
            : transactionRepository.findAll();

    List<TransactionResponse> responses =
        transactions.stream().map(this::mapToResponse).collect(Collectors.toList());

    return ResponseEntity.ok(
        ResponseUtil.success("Transactions retrieved successfully", responses));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get transaction by ID")
  public ResponseEntity<BaseResponse<TransactionResponse>> getTransaction(@PathVariable Long id) {
    return transactionRepository
        .findById(id)
        .map(
            transaction ->
                ResponseEntity.ok(
                    ResponseUtil.success(
                        "Transaction retrieved successfully", mapToResponse(transaction))))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete transaction")
  public ResponseEntity<BaseResponse<Void>> deleteTransaction(@PathVariable Long id) {
    if (transactionRepository.existsById(id)) {
      transactionRepository.deleteById(id);
      return ResponseEntity.ok(ResponseUtil.success("Transaction deleted successfully", null));
    }
    return ResponseEntity.notFound().build();
  }

  private TransactionResponse mapToResponse(Transaction transaction) {
    return TransactionResponse.builder()
        .id(transaction.getId())
        .customerId(transaction.getCustomerId())
        .amount(transaction.getAmount())
        .transactionDate(transaction.getTransactionDate())
        .transactionType(transaction.getTransactionType())
        .processed(transaction.isProcessed())
        .build();
  }
}
