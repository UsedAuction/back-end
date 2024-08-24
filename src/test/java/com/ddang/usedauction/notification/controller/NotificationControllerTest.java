package com.ddang.usedauction.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.domain.NotificationType;
import com.ddang.usedauction.notification.service.NotificationService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 구독 - 성공")
    void subscribeSuccess() throws Exception {
        //given
        long memberId = 1L;
        SseEmitter sseEmitter = new SseEmitter();

        given(notificationService.subscribe(memberId, "")).willReturn(sseEmitter);

        //when
        //then
        mockMvc.perform(
                get("/api/members/notification/subscribe")
                    .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                    .header("Last-Event-ID", ""))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("알림 구독 - 실패")
    void subscribeFail() throws Exception {
        //given
        long memberId = 1L;

        given(notificationService.subscribe(memberId, "")).willThrow(new RuntimeException("예외발생"));

        //when
        //then
        mockMvc.perform(
                get("/api/members/notification/subscribe")
                    .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                    .header("Last-Event-ID", ""))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("알림 전체 목록 조회 - 성공")
    void getNotificationListSuccess() throws Exception {

        //given
        Member member = Member.builder()
            .id(1L)
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

        given(notificationService.getNotificationList(member.getId(), pageable))
            .willReturn(notificationPage);

        //when
        //then
        mockMvc.perform(get("/api/members/notification"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].content").value("알림1"))
            .andExpect(jsonPath("$.content[1].content").value("알림2"))
            .andExpect(jsonPath("$.content[2].content").value("알림3"))
            .andExpect(jsonPath("$.content.size()").value(3));
    }

    @Test
    @DisplayName("알림 전체 목록 조회 - 실패")
    void getNotificationListFail() throws Exception {

        //given
        Member member = Member.builder()
            .id(1L)
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        given(notificationService.getNotificationList(member.getId(), pageable))
            .willThrow(new RuntimeException());

        //when
        //then
        mockMvc.perform(get("/api/members/notification"))
            .andExpect(status().isInternalServerError());
    }
}