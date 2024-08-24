package com.ddang.usedauction.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  /**
   * 403 에러 핸들링
   */
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json;charset=utf-8");

    setResponse(response);

  }

  /**
   * Error 관련 응답 생성 메서드
   */
  private void setResponse(HttpServletResponse response) throws IOException {
    String json = objectMapper.writeValueAsString(
        Map.of(HttpStatus.FORBIDDEN, "권한이 없어서 요청이 거부되었습니다."));

    response.getWriter().write(json);
  }
}
