package com.ddang.usedauction.global.security.jwt;

import com.ddang.usedauction.token.domain.entity.JwtToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

  private final TokenProvider tokenProvider;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    String email = authentication.getName();
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    JwtToken token = tokenProvider.generateToken(email, authorities);

    response.addHeader("Authorization", "Bearer " + token.getAccessToken());
  }
}