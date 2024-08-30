package com.ddang.usedauction.token.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenService(
        @Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

    }

    public void save(String accessToken, String refreshToken, long refreshTokenExpiration) {

        redisTemplate.opsForValue()
            .set("refresh:" + accessToken, refreshToken, refreshTokenExpiration,
                TimeUnit.MILLISECONDS);
    }

    public String findRefreshTokenByAccessToken(String accessToken) {
        return redisTemplate.opsForValue().get("refresh:" + accessToken);
    }

    public void deleteRefreshTokenByAccessToken(String accessToken) {
        redisTemplate.delete("refresh:" + accessToken);
    }

    public void setBlackList(String accessToken, String strAccessToken,
        Long accessTokenExpiration) {
        redisTemplate.opsForValue()
            .set("blacklist:" + accessToken, strAccessToken, accessTokenExpiration,
                TimeUnit.MILLISECONDS);
    }

    public boolean hasKeyBlackList(String accessToken) {
        return redisTemplate.hasKey("blacklist:" + accessToken);
    }
}
