// exception/GlobalExceptionHandler.java
package com.bybygo.loyalty.exception;

import com.bybygo.loyalty.exceptions.BatchJobException;
import com.bybygo.loyalty.model.dto.base.BaseResponse;
import com.bybygo.loyalty.util.ResponseUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BatchJobException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<BaseResponse<Void>> handleBatchJobException(BatchJobException ex) {
    log.error("Batch job error: ", ex);
    return ResponseEntity.badRequest().body(ResponseUtil.error(ex.getMessage(), "BATCH_JOB_ERROR"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<BaseResponse<Void>> handleValidationException(
      MethodArgumentNotValidException ex) {
    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");

    return ResponseEntity.badRequest().body(ResponseUtil.error(errorMessage, "VALIDATION_ERROR"));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(
      ConstraintViolationException ex) {
    String errorMessage =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Constraint violation");

    return ResponseEntity.badRequest()
        .body(ResponseUtil.error(errorMessage, "CONSTRAINT_VIOLATION"));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<BaseResponse<Void>> handleGenericException(Exception ex) {
    log.error("Unexpected error: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseUtil.error("An unexpected error occurred", "INTERNAL_ERROR"));
  }
}
