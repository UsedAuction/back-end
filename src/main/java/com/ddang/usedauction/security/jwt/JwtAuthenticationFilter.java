package com.ddang.usedauction.security.jwt;

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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${spring.jwt.access.expiration}")
    private int accessTokenExpiration;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String accessToken = CookieUtil.getCookieValue(request, "JWT")
            .orElse(null);

        // accessToken 검증
        if (StringUtils.hasText(accessToken)) {
            if (tokenProvider.validateToken(accessToken)) {
                setAuthentication(accessToken);
            } else {
                handleExpiredAccessToken(request, response, accessToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleExpiredAccessToken(HttpServletRequest request,
        HttpServletResponse response, String oldAccessToken) throws IOException {
        // 만료되었으면 accessToken 재발급
        Authentication authentication = tokenProvider.getAuthentication(oldAccessToken);
        String refreshToken = refreshTokenService.findRefreshTokenByAccessToken(
            oldAccessToken);

        // refreshToken 만료되었으면 로그아웃 처리
        if (tokenProvider.isExpiredToken(refreshToken) || refreshToken == null) {
            logout(request, response, oldAccessToken);

            return;
        }

        // 만료되지 않았으면 accessToken 재발급
        String newAccessToken = tokenProvider.reissueAccessToken(authentication.getName(),
            authentication.getAuthorities());
        long refreshTokenExpiration = tokenProvider.getExpiration(refreshToken);
        // Redis accessToken 값 업데이트
        refreshTokenService.deleteRefreshTokenByAccessToken(oldAccessToken);
        refreshTokenService.save(newAccessToken, refreshToken, refreshTokenExpiration);

        setAuthentication(newAccessToken);

        CookieUtil.addCookie(response, "JWT", newAccessToken, accessTokenExpiration);
    }

    // 보안 컨텍스트에 인증 정보 설정 (현재 사용자 인증 정보 갱신)
    private void setAuthentication(String token) {
        Authentication authentication = tokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response,
        String accessToken) {
        // Redis 사용자의 refreshToken 삭제
        refreshTokenService.deleteRefreshTokenByAccessToken(accessToken);

        if (!tokenProvider.isExpiredToken(accessToken)) {
            long accessTokenExpiration = tokenProvider.getExpiration(accessToken);

            refreshTokenService.setBlackList(accessToken, "accessToken",
                accessTokenExpiration);
        }

        // 보안 컨텍스트에서 인증 정보 제거
        SecurityContextHolder.clearContext();

        CookieUtil.deleteCookie(request, response, "JWT");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    }
}

