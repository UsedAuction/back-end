package com.ddang.usedauction.token.dto;

import jakarta.persistence.Id;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(timeToLive = 60 * 60 * 24 * 7)
public class TokenDto implements Serializable {

  @Id
  private String email;
  private String refreshToken;
  private String accessToken;

  public void updateAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
