package com.ddang.usedauction.token.service;

import com.ddang.usedauction.token.dto.TokenDto;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

  private final RedisTemplate<String, Object> redisTemplate;

  public void save(String email, String accessToken, String refreshToken) {
    TokenDto token = TokenDto.builder()
        .email(email)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();

    redisTemplate.opsForValue().set(email, token);
  }

  public TokenDto findTokenByEmail(String email) {
    return (TokenDto) redisTemplate.opsForValue().get(email);
  }

  public void updateToken(TokenDto token) {
    redisTemplate.opsForValue().set(token.getEmail(), token);
  }

  public void deleteRefreshTokenByEmail(String email) {
    redisTemplate.delete(email);
  }

  public void setBlackList(String key, Object o, Long milliSeconds) {
    redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer(o.getClass()));
    redisTemplate.opsForValue().set(key, o, milliSeconds, TimeUnit.MILLISECONDS);
  }

  public boolean hasKeyBlackList(String key) {
    return redisTemplate.hasKey(key);
  }
}
