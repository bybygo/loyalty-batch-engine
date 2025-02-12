package com.bybygo.loyalty.util;

import com.bybygo.loyalty.model.dto.base.BaseResponse;
import java.time.LocalDateTime;

public final class ResponseUtil {

  private ResponseUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static <T> BaseResponse<T> success(String message, T data) {
    return BaseResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now().toString())
        .build();
  }

  public static <T> BaseResponse<T> error(String message, String errorCode) {
    return BaseResponse.<T>builder()
        .success(false)
        .message(message)
        .errorCode(errorCode)
        .timestamp(LocalDateTime.now().toString())
        .build();
  }
}
