package com.ddang.usedauction.security.jwt;

import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String accessToken = tokenProvider.resolveTokenFromRequest(request);

        log.debug("Request URI = {}, Method = {}, Headers = {}",
            request.getRequestURI(), request.getMethod(), request.getHeaderNames());

        log.debug("doFilterInternal accessToken = {}", accessToken);

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

        Cookie cookie = CookieUtil.getCookie(request, "refreshToken")
            .orElse(null);

        // refreshToken 만료되었으면 로그아웃 처리
        if (cookie == null || tokenProvider.isExpiredToken(cookie.getValue())) {

            log.debug("handleExpiredAccessToken cookie = {}",
                cookie != null ? cookie.getValue() : null);
            logout(request, response);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        TokenDto tokenDto = tokenProvider.generateToken(memberIdByToken,
            authentication.getAuthorities());

        setAuthentication(tokenDto.getAccessToken());

        response.setHeader("New-Token", tokenDto.getAccessToken());
        CookieUtil.deleteCookie(request, response, "refreshToken");
        CookieUtil.addCookie(response, "refreshToken", tokenDto.getRefreshToken());
    }

    // 보안 컨텍스트에 인증 정보 설정 (현재 사용자 인증 정보 갱신)
    private void setAuthentication(String token) {
        Authentication authentication = tokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request, response, "refreshToken");

        // 보안 컨텍스트에서 인증 정보 제거
        SecurityContextHolder.clearContext();
    }
}

