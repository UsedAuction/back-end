package com.ddang.usedauction.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.domain.NotificationType;
import com.ddang.usedauction.notification.exception.NotificationBadRequestException;
import com.ddang.usedauction.notification.repository.EmitterRepository;
import com.ddang.usedauction.notification.repository.NotificationRepository;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmitterRepository emitterRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 구독 - 성공")
    void subscribeSuccess() {
        //given
        long memberId = 1L;
        String lastEventId = "123";
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(emitterRepository.save(anyString(), any(SseEmitter.class))).willReturn(sseEmitter);

        //when
        SseEmitter resultEmitter = notificationService.subscribe(memberId, lastEventId);

        //then
        assertNotNull(resultEmitter);
        assertEquals(sseEmitter, resultEmitter);
        verify(emitterRepository).save(anyString(), any(SseEmitter.class));
    }

    @Test
    @DisplayName("알림 구독 - 실패 (emitterRepository 저장 실패)")
    void subscribeFail_emitterRepositorySave() {
        //given
        long memberId = 1L;
        String lastEventId = "123";

        given(emitterRepository.save(anyString(), any(SseEmitter.class)))
            .willThrow(new RuntimeException("emitterRepository 저장 실패"));

        //when
        //then
        assertThrows(RuntimeException.class, () -> notificationService.subscribe(memberId, lastEventId));
    }

    @Test
    @DisplayName("알림 구독 - 실패 (sendNotification 예외 발생)")
    void subscribeFail_sendNotification() throws IOException {
        //given
        long memberId = 1L;
        String lastEventId = "123";
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(emitterRepository.save(anyString(), any(SseEmitter.class))).willReturn(sseEmitter);
        doThrow(new IOException("전송 실패")).when(sseEmitter).send(any(SseEmitter.SseEventBuilder.class));

        ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);

        // when
        NotificationBadRequestException e = assertThrows(NotificationBadRequestException.class, () -> {
            notificationService.subscribe(memberId, lastEventId);
        });

        // then
        assertEquals("전송 실패", e.getMessage());
        verify(emitterRepository, times(1)).deleteById(emitterIdCaptor.capture());
    }

    @Test
    @DisplayName("알림 구독 - 실패 (findAllEventCacheStartWithMemberId 조회 실패)")
    void subscribeFail_findAllEventCacheStartWithMemberId() {
        //given
        long memberId = 1L;
        String lastEventId = "123";
        SseEmitter sseEmitter = mock(SseEmitter.class);

        given(emitterRepository.save(anyString(), any(SseEmitter.class))).willReturn(sseEmitter);
        given(emitterRepository.findAllEventCacheStartWithMemberId(String.valueOf(memberId)))
            .willThrow(new RuntimeException("findAllEventCacheStartWithMemberId 실패"));

        //when
        //then
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> notificationService.subscribe(memberId, lastEventId));
        assertEquals("findAllEventCacheStartWithMemberId 실패", e.getMessage());
    }

    @Test
    @DisplayName("알림 전송 - 성공")
    void sendSuccess() {
        //given
        Member member = mock(Member.class);
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";
        Notification notification = Notification.builder()
            .content(content)
            .notificationType(notificationType)
            .member(member)
            .build();

        Map<String, SseEmitter> emitters = new HashMap<>();
        SseEmitter sseEmitter1 = new SseEmitter();
        SseEmitter sseEmitter2 = new SseEmitter();
        emitters.put("1_1", sseEmitter1);
        emitters.put("1_2", sseEmitter2);

        given(member.getId()).willReturn(1L);
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId("1")).willReturn(emitters);

        //when
        notificationService.send(member, notificationType, content);

        //then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId("1");
        verify(emitterRepository, times(2)).saveEventCache(anyString(), eq(notification));
    }

    @Test
    @DisplayName("알림 전송 - 실패")
    void sendFail() throws IOException {
        //given
        Member member = mock(Member.class);
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";
        Notification notification = Notification.builder()
            .content(content)
            .notificationType(notificationType)
            .member(member)
            .build();

        SseEmitter emitter = mock(SseEmitter.class);
        Map<String, SseEmitter> emitters = Collections.singletonMap("1_1234", emitter);

        given(member.getId()).willReturn(1L);
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);
        given(emitterRepository.findAllEmitterStartWithMemberId("1")).willReturn(emitters);

        doThrow(new IOException("전송 실패")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        ArgumentCaptor<String> emitterIdCaptor = ArgumentCaptor.forClass(String.class);

        //when
        NotificationBadRequestException e = assertThrows(NotificationBadRequestException.class, () -> {
            notificationService.send(member, notificationType, content);
        });

        // then
        assertEquals("전송 실패", e.getMessage());
        verify(emitterRepository, times(1)).deleteById(emitterIdCaptor.capture());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emitterRepository, times(1)).findAllEmitterStartWithMemberId("1");
        verify(emitterRepository, times(1)).saveEventCache(anyString(), eq(notification));
        verify(emitterRepository, times(1)).deleteById("1_1234");
    }

    @Test
    @DisplayName("알림 전송 - 실패 (notificationRepository 저장 실패)")
    void sendFail_notificationRepository() {
        //given
        Member member = mock(Member.class);
        NotificationType notificationType = NotificationType.DONE;
        String content = "경매가 종료되었습니다.";

        given(notificationRepository.save(any(Notification.class))).willThrow(new RuntimeException("저장 실패"));

        //when
        RuntimeException e = assertThrows(RuntimeException.class,
            () -> notificationService.send(member, notificationType, content));

        //then
        assertEquals("저장 실패", e.getMessage());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emitterRepository, times(0)).findAllEmitterStartWithMemberId("1");
        verify(emitterRepository, times(0)).saveEventCache(anyString(), any(Notification.class));
    }
}