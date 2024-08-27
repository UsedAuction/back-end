package com.ddang.usedauction.security.jwt;

import com.ddang.usedauction.security.auth.PrincipalDetails;
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

    @Value("${spring.jwt.refresh.expiration}")
    private int refreshTokenExpirationValue;
    private static final String URI = "localhost:5173";
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        TokenDto token = tokenProvider.generateToken(details.getName(), authorities);

        long refreshTokenExpiration = tokenProvider.getExpiration(token.getRefreshToken());
        refreshTokenService.save(token.getAccessToken(), token.getRefreshToken(),
            refreshTokenExpiration);

        CookieUtil.addCookie(response, "refreshToken", token.getRefreshToken(),
            refreshTokenExpirationValue);
        response.sendRedirect(
            URI + "?accessToken=" + token.getAccessToken() + "&memberId=" + details.getName());
    }
}