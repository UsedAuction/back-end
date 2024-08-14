package com.ddang.usedauction.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
}