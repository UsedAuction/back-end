package com.ddang.usedauction.security.jwt;

import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.ddang.usedauction.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${spring.jwt.refresh.expiration}")
    int refreshExpiration;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String accessToken = tokenProvider.resolveTokenFromRequest(request);

        log.info("accessToken = {}", accessToken);

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
        HttpServletResponse response, String oldAccessToken) {
        // 만료되었으면 accessToken 재발급
        Authentication authentication = tokenProvider.getAuthentication(oldAccessToken);
        String memberIdByToken = tokenProvider.getMemberIdByToken(oldAccessToken);
        String refreshToken = refreshTokenService.findRefreshTokenByAccessToken(
            oldAccessToken);
        Cookie cookie = CookieUtil.getCookie(request, "refreshToken")
            .orElse(null);

        // refreshToken 만료되었으면 로그아웃 처리
        if (tokenProvider.isExpiredToken(refreshToken) || refreshToken == null
            || cookie == null || !refreshToken.equals(
            cookie.getValue())) {
            logout(request, response, oldAccessToken);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        // 만료되지 않았으면 accessToken, refreshToken 재발급
        TokenDto tokenDto = tokenProvider.generateToken(memberIdByToken,
            authentication.getAuthorities());
        // Redis accessToken 값 업데이트
        refreshTokenService.deleteRefreshTokenByAccessToken(oldAccessToken);
        refreshTokenService.save(tokenDto.getAccessToken(), tokenDto.getRefreshToken(),
            refreshExpiration);

        setAuthentication(tokenDto.getAccessToken());

        response.setHeader("New-Token", tokenDto.getAccessToken());
        CookieUtil.deleteCookie(request, response, "refreshToken");
        CookieUtil.addCookie(response, "refreshToken", tokenDto.getRefreshToken(),
            refreshExpiration);
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

        CookieUtil.deleteCookie(request, response, "refreshToken");

        // 보안 컨텍스트에서 인증 정보 제거
        SecurityContextHolder.clearContext();
    }
}

