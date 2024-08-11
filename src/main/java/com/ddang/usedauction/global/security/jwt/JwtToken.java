package com.ddang.usedauction.global.security.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtToken {

  private String grantType;
  private String accessToken;
  private String refreshToken;
}
