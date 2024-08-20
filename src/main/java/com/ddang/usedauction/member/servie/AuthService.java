package com.ddang.usedauction.member.servie;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.security.jwt.exception.CustomJwtException;
import com.ddang.usedauction.security.jwt.exception.JwtErrorCode;
import com.ddang.usedauction.token.dto.AccessTokenDto;
import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

  private final RefreshTokenService refreshTokenService;
  private final TokenProvider tokenProvider;
  private final MemberRepository memberRepository;

  /**
   * 토큰 재발급
   *
   * @param accessToken 만료된 토큰
   * @return
   */
  @Transactional
  public String reissueToken(AccessTokenDto dto) {
    String email = tokenProvider.getEmailByToken(dto.getAccessToken());

    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

    TokenDto token = refreshTokenService.findTokenByEmail(email);
    String oldAccessToken = token.getAccessToken();

    if (tokenProvider.isExpiredToken(oldAccessToken) &&
        !tokenProvider.isExpiredToken(token.getRefreshToken())) {
      String newAccessToken = tokenProvider.reissueAccessToken(member.getEmail(),
          List.of(new SimpleGrantedAuthority(member.getRole().toString())));
      token.updateAccessToken(newAccessToken);
      refreshTokenService.updateToken(token);

      return newAccessToken;
    }
    throw new CustomJwtException(JwtErrorCode.EXPIRED_REFRESH_TOKEN);
  }

  /**
   * @param accessToken 사용자의 토큰
   */
  @Transactional
  public void deleteToken(AccessTokenDto dto) {
    if (!tokenProvider.validateToken(dto.getAccessToken())) {
      throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
    }

    Long expiration = tokenProvider.getExpiration(dto.getAccessToken());
    String email = tokenProvider.getEmailByToken(dto.getAccessToken());
    TokenDto token = refreshTokenService.findTokenByEmail(email);

    // accessToken 유효시간만큼 blackList 에 저장
    refreshTokenService.setBlackList(dto.getAccessToken(), token, expiration);
    refreshTokenService.deleteRefreshTokenByEmail(token.getEmail());
  }
}
