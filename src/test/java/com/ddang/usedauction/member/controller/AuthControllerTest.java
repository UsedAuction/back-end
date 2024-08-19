package com.ddang.usedauction.member.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.member.servie.AuthService;
import com.ddang.usedauction.token.dto.AccessTokenDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  AuthService authService;

  @Test
  @WithMockUser
  @DisplayName("토큰 재발급 Controller")
  void reissueAccessToken() throws Exception {
    AccessTokenDto request = AccessTokenDto.builder()
        .accessToken("oldAccessToken")
        .build();
    AccessTokenDto response = AccessTokenDto.builder()
        .accessToken("newAccessToken")
        .build();

    when(authService.reissueToken(argThat(dto ->
        dto.getAccessToken().equals("oldAccessToken")))).thenReturn(response.getAccessToken());

    mockMvc.perform(post("/api/auth/reissue")
            .with(csrf().asHeader())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .cookie(new Cookie("JWT", "oldAccessToken")))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
        .andReturn();

    verify(authService).reissueToken(argThat(dto ->
        dto.getAccessToken().equals("oldAccessToken")));
  }

  @Test
  @WithMockUser
  @DisplayName("로그아웃")
  void logout() throws Exception {
    AccessTokenDto request = AccessTokenDto.builder()
        .accessToken("accessToken")
        .build();

    mockMvc.perform(post("/api/auth/logout")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .cookie(new Cookie("JWT", "accessToken"))
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isOk());

    verify(authService).deleteToken(argThat(dto ->
        dto.getAccessToken().equals("accessToken")));
  }
}