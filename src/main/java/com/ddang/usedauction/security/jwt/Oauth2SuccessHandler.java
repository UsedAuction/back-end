package com.ddang.usedauction.security.jwt;

import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.ddang.usedauction.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

  @Value("${spring.datasource.jwt.access.expiration}")
  private int accessTokenExpiration;
  private static final String URI = "/";
  private final TokenProvider tokenProvider;
  private final RefreshTokenService RefreshTokenService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    String email = authentication.getName();
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    TokenDto token = tokenProvider.generateToken(email, authorities);
    RefreshTokenService.save(email, token.getAccessToken(), token.getRefreshToken());

    CookieUtil.addCookie(response, "JWT", token.getAccessToken(), accessTokenExpiration);
//    테스트하기 위해 주석처리
//    response.sendRedirect(URI);
  }
}