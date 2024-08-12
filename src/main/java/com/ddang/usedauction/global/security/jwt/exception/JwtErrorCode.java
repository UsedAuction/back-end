package com.ddang.usedauction.global.security.jwt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtErrorCode {
  INVALID_TOKEN("잘못된 토큰입니다."),
  INVALID_REFRESH_TOKEN("잘못된 Refresh 토큰입니다."),
  EXPIRED_TOKEN("만료된 토큰입니다."),
  UNSUPPORTED_TOKEN("지원되지 않는 토큰입니다."),
  EMPTY_CLAIM("JWT 클레임 문자열이 비어 있습니다.");

  private final String message;
}
