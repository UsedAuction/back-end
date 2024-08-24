package com.ddang.usedauction.security.jwt;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.member.controller.AuthController;
import com.ddang.usedauction.member.servie.AuthService;
import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest({AuthController.class, SecurityConfig.class})
class JwtExceptionHandlerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  WebApplicationContext context;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  TokenProvider tokenProvider;

  @MockBean
  RefreshTokenService refreshTokenService;

  @MockBean
  PrincipalOauth2UserService principalOauth2UserService;

  @MockBean
  Oauth2SuccessHandler oauth2SuccessHandler;

  @MockBean
  Oauth2FailureHandler oauth2FailureHandler;

  @MockBean
  AuthService authService;


  @BeforeEach
  void before() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void Unauthorized() throws Exception {
    mockMvc.perform(get("/api/auth/get")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.UNAUTHORIZED").value("로그인이 필요합니다."));
  }

  @Test
  @WithMockUser(roles = "USER")
  void accessDeniedHandler() throws Exception {
    mockMvc.perform(get("/api/auth/get")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.FORBIDDEN").value("권한이 없어서 요청이 거부되었습니다."));
  }
}