package com.ddang.usedauction.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.domain.NotificationType;
import com.ddang.usedauction.notification.service.NotificationService;
import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.security.jwt.Oauth2SuccessHandler;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.service.RefreshTokenService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest({NotificationController.class, SecurityConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private PrincipalOauth2UserService principalOauth2UserService;

    @MockBean
    private Oauth2SuccessHandler oauth2SuccessHandler;

    @MockBean
    private Oauth2FailureHandler oauth2FailureHandler;

    @Test
    @WithCustomMockUser
    @DisplayName("알림 구독 - 성공")
    void subscribeSuccess() throws Exception {

        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .build();

        SseEmitter sseEmitter = new SseEmitter();

        given(notificationService.subscribe(member.getEmail(), "")).willReturn(sseEmitter);

        //when
        //then
        mockMvc.perform(
                get("/api/members/notification/subscribe")
                    .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                    .header("Last-Event-ID", ""))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("알림 구독 - 실패(인증되지 않은 사용자)")
    void subscribeFail_1() throws Exception {

        //given
        //when
        //then
        mockMvc.perform(
                get("/api/members/notification/subscribe")
                    .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                    .header("Last-Event-ID", ""))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("알림 전체 목록 조회 - 성공")
    void getNotificationListSuccess() throws Exception {

        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        Page<Notification> notificationPage = new PageImpl<>(
            List.of(
                Notification.builder()
                    .id(1L)
                    .content("알림1")
                    .notificationType(NotificationType.DONE)
                    .member(member)
                    .build(),
                Notification.builder()
                    .id(2L)
                    .content("알림2")
                    .notificationType(NotificationType.CONFIRM)
                    .member(member)
                    .build(),
                Notification.builder()
                    .id(3L)
                    .content("알림3")
                    .notificationType(NotificationType.QUESTION)
                    .member(member)
                    .build()),
            pageable,
            3
        );

        given(notificationService.getNotificationList(member.getEmail(), pageable))
            .willReturn(notificationPage);

        //when
        //then
        mockMvc.perform(
                get("/api/members/notification")
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].content").value("알림1"))
            .andExpect(jsonPath("$.content[1].content").value("알림2"))
            .andExpect(jsonPath("$.content[2].content").value("알림3"))
            .andExpect(jsonPath("$.content.size()").value(3));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("알림 전체 목록 조회 - 실패(인증되지 않은 사용자)")
    void getNotificationListFail() throws Exception {

        //given
        //when
        //then
        mockMvc.perform(
                get("/api/members/notification/subscribe")
                    .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                    .header("Last-Event-ID", ""))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}