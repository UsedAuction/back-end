package com.ddang.usedauction.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Oauth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("소셜 로그인에 실패했습니다.");
        log.error("소셜 로그인에 실패했습니다. 에러 메시지 : {}", exception.getMessage());
    }
}
