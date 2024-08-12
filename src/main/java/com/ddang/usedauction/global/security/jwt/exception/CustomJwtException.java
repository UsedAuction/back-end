package com.ddang.usedauction.global.security.jwt.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class CustomJwtException extends RuntimeException {

  private final JwtErrorCode errorCode;
  private final String message;

  public CustomJwtException(JwtErrorCode errorCode) {
    this.errorCode = errorCode;
    this.message = errorCode.getMessage();
  }
}
