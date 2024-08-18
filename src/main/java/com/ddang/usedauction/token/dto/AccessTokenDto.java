package com.ddang.usedauction.token.dto;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AccessTokenDto implements Serializable {


  private String accessToken;

  public static AccessTokenDto from(String accessToken) {
    return new AccessTokenDto(accessToken);
  }
}
