package com.ddang.usedauction.security.jwt;

import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.ddang.usedauction.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Value("${spring.jwt.access.expiration}")
  private int accessTokenExpiration;
  private final TokenProvider tokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = CookieUtil.getCookieValue(request, "JWT")
        .orElseThrow(() -> new RuntimeException("쿠키가 존재하지 않습니다."));
    // accessToken 검증
    if (token != null && tokenProvider.validateToken(token)) {
      setAuthentication(token);
    } else if (token != null && !tokenProvider.validateToken(token)) {
      // 만료되었으면 accessToken 재발급
      Authentication authentication = tokenProvider.getAuthentication(token);
      TokenDto dto = refreshTokenService.findTokenByEmail(authentication.getName());

      // refreshToken 만료되었으면 로그아웃 처리
      if (tokenProvider.isExpiredToken(dto.getRefreshToken())) {
        logout(request, response, dto.getEmail());
        return;
      }

      // 만료되지 않았으면 accessToken 재발급
      String newAccessToken = tokenProvider.reissueAccessToken(authentication.getName(),
          authentication.getAuthorities());
      // Redis accessToken 값 업데이트
      dto.updateAccessToken(newAccessToken);
      refreshTokenService.updateToken(dto);

      if (StringUtils.hasText(newAccessToken)) {
        setAuthentication(newAccessToken);
      }

      CookieUtil.addCookie(response, "JWT", newAccessToken, accessTokenExpiration);
    }
    filterChain.doFilter(request, response);
  }

  // 보안 컨텍스트에 인증 정보 설정 (현재 사용자 인증 정보 갱신)
  private void setAuthentication(String token) {
    Authentication authentication = tokenProvider.getAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void logout(HttpServletRequest request, HttpServletResponse response, String email) {
    // Redis 사용자의 refreshToken 삭제
    refreshTokenService.deleteRefreshTokenByEmail(email);
    // 보안 컨텍스트에서 인증 정보 제거
    SecurityContextHolder.clearContext();

    CookieUtil.deleteCookie(request, response, "JWT");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }
}

