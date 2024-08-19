package com.ddang.usedauction.security.jwt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode {
  INVALID_TOKEN("잘못된 토큰입니다."),
  EXPIRED_TOKEN("만료된 토큰입니다."),
  EXPIRED_REFRESH_TOKEN("만료된 리프레쉬 토큰입니다."),
  UNSUPPORTED_TOKEN("지원되지 않는 토큰입니다.");

  private final String message;
}
