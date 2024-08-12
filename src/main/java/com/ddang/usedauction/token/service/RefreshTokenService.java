package com.ddang.usedauction.token.service;

import com.ddang.usedauction.global.security.jwt.exception.CustomJwtException;
import com.ddang.usedauction.global.security.jwt.exception.JwtErrorCode;
import com.ddang.usedauction.member.domain.entity.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.token.domain.entity.JwtToken;
import jakarta.persistence.EntityNotFoundException;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

  private final long refreshTokenExpiration;
  private final MemberRepository memberRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  public RefreshTokenService(@Value("${spring.datasource.jwt.refresh.expiration}")
  long refreshTokenExpiration,
      MemberRepository memberRepository
      , RedisTemplate<String, Object> redisTemplate) {
    this.refreshTokenExpiration = refreshTokenExpiration;
    this.memberRepository = memberRepository;
    this.redisTemplate = redisTemplate;
  }

  public void save(String email, String accessToken, String refreshToken) {
    JwtToken token = JwtToken.builder()
        .email(email)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();

    redisTemplate.opsForValue().set(refreshToken, token
        , refreshTokenExpiration, TimeUnit.MILLISECONDS);
  }

  public Member findMemberByRefreshToken(String refreshToken) {
    JwtToken token = findTokenByRefreshToken(refreshToken);
    if (token.getExpiration() > 0) {
      return memberRepository.findByEmail(token.getEmail())
          .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
    }
    throw new CustomJwtException(JwtErrorCode.INVALID_REFRESH_TOKEN);
  }

  public JwtToken findTokenByRefreshToken(String refreshToken) {
    JwtToken token = (JwtToken) redisTemplate.opsForValue().get(refreshToken);
    if (token != null) {
      return token;
    }
    throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
  }

  public void updateToken(JwtToken token) {
    redisTemplate.opsForValue().set(token.getRefreshToken(), token,
        token.getExpiration(), TimeUnit.MILLISECONDS);
  }
}
