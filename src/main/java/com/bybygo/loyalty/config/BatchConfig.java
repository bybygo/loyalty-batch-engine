package com.bybygo.loyalty.config;

import com.bybygo.loyalty.batch.CustomerRewardsWriter;
import com.bybygo.loyalty.batch.TransactionProcessor;
import com.bybygo.loyalty.batch.validator.CustomJobParametersValidator;
import com.bybygo.loyalty.constants.BatchConstants;
import com.bybygo.loyalty.model.entity.CustomerRewards;
import com.bybygo.loyalty.model.entity.Transaction;
import com.bybygo.loyalty.repository.TransactionRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

  private final CustomJobParametersValidator jobParametersValidator;
  private final TransactionProcessor transactionProcessor;
  private final TransactionRepository transactionRepository;

  @Bean
  public Job processTransactionsJob(JobRepository jobRepository, Step processTransactionsStep) {
    return new JobBuilder(BatchConstants.TRANSACTION_PROCESSING_JOB, jobRepository)
        .validator(jobParametersValidator)
        .start(processTransactionsStep)
        .build();
  }

  @Bean
  public Step processTransactionsStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder(BatchConstants.TRANSACTION_PROCESSING_STEP, jobRepository)
        .<Transaction, CustomerRewards>chunk(BatchConstants.DEFAULT_CHUNK_SIZE, transactionManager)
        .reader(transactionReader())
        .processor(transactionProcessor)
        .writer(customerRewardsWriter())
        .build();
  }

  @Bean
  public RepositoryItemReader<Transaction> transactionReader() {
    Map<String, Sort.Direction> sortConfig = new HashMap<>();
    sortConfig.put("id", Sort.Direction.ASC);
    sortConfig.put("transactionDate", Sort.Direction.ASC);

    return new RepositoryItemReaderBuilder<Transaction>()
        .name("transactionReader")
        .repository(transactionRepository)
        .methodName("findByProcessedFalse")
        .pageSize(BatchConstants.DEFAULT_CHUNK_SIZE)
        .sorts(sortConfig)
        .maxItemCount(10000)
        .saveState(true)
        .build();
  }

  @Bean
  public CustomerRewardsWriter customerRewardsWriter() {
    return new CustomerRewardsWriter();
  }
}
