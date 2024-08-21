package com.ddang.usedauction.member.servie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.domain.enums.Role;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.security.jwt.exception.CustomJwtException;
import com.ddang.usedauction.security.jwt.exception.JwtErrorCode;
import com.ddang.usedauction.token.dto.AccessTokenDto;
import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  RefreshTokenService refreshTokenService;

  @Mock
  MemberRepository memberRepository;

  @Mock
  TokenProvider tokenProvider;

  @InjectMocks
  AuthService authService;

  @Captor
  ArgumentCaptor<TokenDto> tokenDtoCaptor;


  String refreshToken = "refreshToken";
  String oldAccessToken = "oldAccessToken";
  String newAccessToken = "newAccessToken";

  Member member;
  TokenDto tokenDto;

  @BeforeEach
  void setUp() {
    member = Member.builder()
        .email("test@gmail.com")
        .role(Role.ROLE_USER)
        .build();

    tokenDto = TokenDto.builder()
        .email(member.getEmail())
        .accessToken(oldAccessToken)
        .refreshToken(refreshToken)
        .build();
  }

  @Test
  @DisplayName("토큰 재발급")
  void reissueToken() {
    //given
    ArgumentCaptor<TokenDto> tokenDtoCaptor = ArgumentCaptor.forClass(TokenDto.class);

    when(tokenProvider.getEmailByToken(oldAccessToken)).thenReturn(member.getEmail());
    when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
    when(refreshTokenService.findTokenByEmail(member.getEmail())).thenReturn(tokenDto);
    when(tokenProvider.isExpiredToken(oldAccessToken)).thenReturn(true);
    when(tokenProvider.isExpiredToken(refreshToken)).thenReturn(false);
    when(tokenProvider.reissueAccessToken(member.getEmail(),
        List.of(new SimpleGrantedAuthority(member.getRole().toString()))))
        .thenReturn(newAccessToken);

    //when
    AccessTokenDto response = AccessTokenDto.from(authService.reissueToken(
        AccessTokenDto.builder()
            .accessToken(oldAccessToken)
            .build()));

    // then
    verify(refreshTokenService).updateToken(tokenDtoCaptor.capture());
    TokenDto capturedTokenDto = tokenDtoCaptor.getValue();

    assertEquals(newAccessToken, capturedTokenDto.getAccessToken());
    assertEquals(refreshToken, capturedTokenDto.getRefreshToken());
    assertEquals(newAccessToken, response.getAccessToken());
  }

  @Test
  @DisplayName("토큰 재발급 - 만료된 리프레쉬 토큰 실패")
  void reissueToken_Failure() {
    AccessTokenDto dto = AccessTokenDto.builder()
        .accessToken(oldAccessToken)
        .build();

    when(tokenProvider.getEmailByToken(oldAccessToken)).thenReturn(member.getEmail());
    when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
    when(refreshTokenService.findTokenByEmail(member.getEmail())).thenReturn(tokenDto);
    when(tokenProvider.isExpiredToken(oldAccessToken)).thenReturn(true);
    when(tokenProvider.isExpiredToken(refreshToken)).thenReturn(true);

    CustomJwtException exception = assertThrows(CustomJwtException.class, () -> {
      authService.reissueToken(dto);
    });

    assertEquals(JwtErrorCode.EXPIRED_REFRESH_TOKEN, exception.getErrorCode());
    verify(refreshTokenService, never()).updateToken(tokenDto);
  }

  @Test
  @DisplayName("토큰 삭제 - 블랙리스트 등록, redis에서 삭제")
  void deleteToken() {
    AccessTokenDto dto = AccessTokenDto.builder()
        .accessToken(newAccessToken)
        .build();
    TokenDto tokenDto = TokenDto.builder()
        .email("test@example.com")
        .accessToken("blacklistedAccessToken")
        .refreshToken("blacklistedRefreshToken")
        .build();
    when(tokenProvider.validateToken(dto.getAccessToken())).thenReturn(true);
    when(tokenProvider.getExpiration(dto.getAccessToken())).thenReturn(604800000L);
    when(tokenProvider.getEmailByToken(dto.getAccessToken())).thenReturn(member.getEmail());
    when(refreshTokenService.findTokenByEmail(member.getEmail())).thenReturn(tokenDto);

    authService.deleteToken(dto);

    verify(refreshTokenService).setBlackList(dto.getAccessToken(), tokenDto, 604800000L);
    verify(refreshTokenService).deleteRefreshTokenByEmail(tokenDto.getEmail());
  }
}