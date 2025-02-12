package com.bybygo.loyalty.constants;

public final class BatchConstants {

  // Job Parameters
  public static final String JOB_PARAM_ID = "jobId";
  public static final String JOB_PARAM_DATE = "processDate";
  public static final String JOB_PARAM_DESC = "description";

  // Job Names
  public static final String TRANSACTION_PROCESSING_JOB = "processTransactionsJob";
  public static final String REWARD_CALCULATION_JOB = "rewardCalculationJob";

  // Step Names
  public static final String TRANSACTION_PROCESSING_STEP = "processTransactionsStep";
  public static final String REWARD_CALCULATION_STEP = "rewardCalculationStep";

  // Chunk Sizes
  public static final int DEFAULT_CHUNK_SIZE = 100;

  // Date Formats
  public static final String DATE_FORMAT = "yyyy-MM-dd";

  private BatchConstants() {
    throw new IllegalStateException("Utility class");
  }
}
