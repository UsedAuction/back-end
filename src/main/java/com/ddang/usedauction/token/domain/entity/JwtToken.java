package com.ddang.usedauction.token.domain.entity;

import jakarta.persistence.Id;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtToken {

  @Id
  private String email;
  private String grantType;
  private String accessToken;
  private String refreshToken;

  @TimeToLive(unit = TimeUnit.MILLISECONDS)
  private long expiration;

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
