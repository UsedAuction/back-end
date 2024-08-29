package com.ddang.usedauction.mail.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.mail.dto.EmailCheckDto;
import com.ddang.usedauction.mail.dto.EmailSendDto;
import com.ddang.usedauction.mail.service.MailCheckService;
import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.security.jwt.Oauth2SuccessHandler;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({MailController.class, SecurityConfig.class})
class MailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    TokenProvider tokenProvider;

    @MockBean
    MailCheckService mailCheckService;

    @MockBean
    RefreshTokenService refreshTokenService;

    @MockBean
    PrincipalOauth2UserService principalOauth2UserService;

    @MockBean
    Oauth2SuccessHandler oauth2SuccessHandler;

    @MockBean
    Oauth2FailureHandler oauth2FailureHandler;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("이메일 전송 - 성공")
    void sendMail() throws Exception {
        EmailSendDto dto = EmailSendDto.builder()
            .email("test@mail.com")
            .build();

        mockMvc.perform(post("/api/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 전송 - 실패: 유효성 검사 실패")
    void sendMailFail() throws Exception {
        EmailSendDto dto = EmailSendDto.builder()
            .email("@mail.com")
            .build();

        mockMvc.perform(post("/api/mail/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증번호 확인 - 성공")
    void authCheck() throws Exception {
        EmailCheckDto dto = EmailCheckDto.builder()
            .email("test@mail.com")
            .authNum("1234")
            .build();

        when(mailCheckService.checkAuthNum(
            argThat(arg -> arg.equals("test@mail.com")),
            argThat(arg -> arg.equals("1234"))
        )).thenReturn(true);

        mockMvc.perform(post("/api/mail/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증번호 확인 - 실패: 잘못된 인증번호 입력")
    void authCheckFail1() throws Exception {
        EmailCheckDto dto = EmailCheckDto.builder()
            .email("test@mail.com")
            .authNum("1234")
            .build();

        when(mailCheckService.checkAuthNum(
            argThat(arg -> arg.equals("test@mail.com")),
            argThat(arg -> arg.equals("wrong"))
        )).thenReturn(false);

        mockMvc.perform(post("/api/mail/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증번호 확인 - 실패: 이메일 형식 불일치")
    void authCheckFail2() throws Exception {
        EmailCheckDto dto = EmailCheckDto.builder()
            .email("tesmail.com")
            .authNum("1234")
            .build();

        when(mailCheckService.checkAuthNum(
            argThat(arg -> arg.equals("tesmail.com")),
            argThat(arg -> arg.equals("1234"))
        )).thenReturn(true);

        mockMvc.perform(post("/api/mail/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}