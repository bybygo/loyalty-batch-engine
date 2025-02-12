package com.bybygo.loyalty.exceptions;

public class BatchJobException extends RuntimeException {

  public BatchJobException(String message) {
    super(message);
  }

  public BatchJobException(String message, Throwable cause) {
    super(message, cause);
  }
}
