package com.bybygo.loyalty.batch.validator;

import com.bybygo.loyalty.constants.BatchConstants;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.stereotype.Component;

@Component
public class CustomJobParametersValidator implements JobParametersValidator {

  private static final int MAX_DESCRIPTION_LENGTH = 255;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  @Override
  public void validate(JobParameters parameters) throws JobParametersInvalidException {
    List<String> validationErrors = validateParameters(parameters);

    if (!validationErrors.isEmpty()) {
      throw new JobParametersInvalidException(String.join("; ", validationErrors));
    }
  }

  private List<String> validateParameters(JobParameters parameters) {
    List<String> errors = new ArrayList<>();

    validateJobId(parameters, errors);
    validateProcessDate(parameters, errors);
    validateDescription(parameters, errors);

    return errors;
  }

  private void validateJobId(JobParameters parameters, List<String> errors) {
    String jobId = parameters.getString(BatchConstants.JOB_PARAM_ID);
    if (jobId == null || jobId.trim().isEmpty()) {
      errors.add("jobId parameter is required");
    }
  }

  private void validateProcessDate(JobParameters parameters, List<String> errors) {
    String processDateStr = parameters.getString(BatchConstants.JOB_PARAM_DATE);
    if (processDateStr == null || processDateStr.trim().isEmpty()) {
      errors.add("processDate parameter is required");
      return;
    }

    try {
      LocalDate processDate = LocalDate.parse(processDateStr, DATE_FORMATTER);
      validateProcessDateRange(processDate, errors);
    } catch (DateTimeParseException e) {
      errors.add("Invalid processDate format. Expected format: yyyy-MM-dd");
    }
  }

  private void validateProcessDateRange(LocalDate processDate, List<String> errors) {
    LocalDate now = LocalDate.now();
    LocalDate maxFutureDate = now.plusDays(7);
    LocalDate minPastDate = now.minusMonths(3);

    if (processDate.isAfter(maxFutureDate)) {
      errors.add("processDate cannot be more than 7 days in the future");
    }
    if (processDate.isBefore(minPastDate)) {
      errors.add("processDate cannot be more than 3 months in the past");
    }
  }

  private void validateDescription(JobParameters parameters, List<String> errors) {
    String description = parameters.getString(BatchConstants.JOB_PARAM_DESC);
    if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
      errors.add(
          String.format("Description must not exceed %d characters", MAX_DESCRIPTION_LENGTH));
    }
  }
}
