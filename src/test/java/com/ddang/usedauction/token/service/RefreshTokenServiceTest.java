package com.ddang.usedauction.token.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.token.dto.TokenDto;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private ValueOperations<String, Object> valueOperations;

  @InjectMocks
  private RefreshTokenService refreshTokenService;

  private long refreshTokenExpiration = 604800000L;

  @Test
  @DisplayName("RefreshToken 저장")
  void save() {
    // given
    TokenDto token = TokenDto.builder()
        .email("test@gmail.com")
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    // when
    refreshTokenService.save(token.getEmail(), token.getAccessToken(), token.getRefreshToken());

    // then
    assertEquals("test@gmail.com", token.getEmail());
  }

  @Test
  @DisplayName("회원 찾기")
  void findTokenByEmail() {
    //given
    TokenDto token = TokenDto.builder()
        .email("test@gmail.com")
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(redisTemplate.opsForValue().get("test@gmail.com")).thenReturn(token);
    //when
    TokenDto result = refreshTokenService.findTokenByEmail("test@gmail.com");
    //then
    assertEquals(token, result);
    assertEquals(token.getRefreshToken(), result.getRefreshToken());
    assertEquals(token.getAccessToken(), result.getAccessToken());
  }

  @Test
  @DisplayName("토큰 재발급")
  void updateToken() {
    //given
    TokenDto token = TokenDto.builder()
        .email("test@gmail.com")
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    //when
    refreshTokenService.updateToken(token);
    //then
    verify(valueOperations).set(eq(token.getEmail()), eq(token));
  }

  @Test
  @DisplayName("리프레쉬 토큰 삭제")
  void deleteRefreshTokenByEmail() {
    //given
    String refreshToken = "refreshToken";
    //when
    refreshTokenService.deleteRefreshTokenByEmail(refreshToken);
    //then
    verify(redisTemplate, times(1)).delete(refreshToken);
  }

  @Test
  @DisplayName("블랙리스트 등록")
  void setBlackList() {
    //given
    String key = "blacklist_key";
    String value = "blacklisted_value";
    Long milliSeconds = 60000L;

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    ArgumentCaptor<Jackson2JsonRedisSerializer> serializerCaptor = ArgumentCaptor.forClass(
        Jackson2JsonRedisSerializer.class);

    //when
    refreshTokenService.setBlackList(key, value, milliSeconds);
    //then
    verify(redisTemplate).setValueSerializer(serializerCaptor.capture());
    verify(valueOperations).set(eq(key), eq(value), eq(milliSeconds), eq(TimeUnit.MILLISECONDS));

    assertTrue(serializerCaptor.getValue() instanceof Jackson2JsonRedisSerializer);
  }

  @Test
  @DisplayName("블랙리스트 키 존재 여부 확인")
  void hasKeyBlackList() {
    //given
    String key = "blacklist_key";
    when(redisTemplate.hasKey(key)).thenReturn(true);
    //when
    boolean result = refreshTokenService.hasKeyBlackList(key);
    //then
    assertTrue(result, "블랙리스트 키가 존재해야 한다.");
    verify(redisTemplate, times(1)).hasKey(key);
  }
}