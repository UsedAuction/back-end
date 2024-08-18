package com.ddang.usedauction.security.jwt.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomJwtException extends RuntimeException {

  private JwtErrorCode errorCode;
  private String message;

  public CustomJwtException(JwtErrorCode errorCode) {
    this.errorCode = errorCode;
    this.message = errorCode.getMessage();
  }
}
