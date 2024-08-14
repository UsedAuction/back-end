package com.ddang.usedauction.notification.service;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.domain.NotificationType;
import com.ddang.usedauction.notification.dto.NotificationDto;
import com.ddang.usedauction.notification.exception.NotificationBadRequestException;
import com.ddang.usedauction.notification.repository.EmitterRepository;
import com.ddang.usedauction.notification.repository.NotificationRepository;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 유효 시간

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;

    // 알림 구독
    public SseEmitter subscribe(long memberId, String lastEventId) {

        String emitterId = memberId + "_" + System.currentTimeMillis(); // 유실된 데이터의 시점을 알기 위해 시간을 붙임
        SseEmitter sseEmitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        // 콜백
        sseEmitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        sseEmitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
        sseEmitter.onError((e) -> emitterRepository.deleteById(emitterId));

        // 503 에러방지를 위한 더미 이벤트 전송
        sendNotification(sseEmitter, emitterId, "더미 이벤트 전송 완료 / 회원 id: " + memberId);

        // 받지 못한 알림이 있으면 보내주기
        if (!lastEventId.isEmpty()) {
            Map<String, Object> cacheEvents = emitterRepository.findAllEventCacheStartWithMemberId(
                String.valueOf(memberId));
            cacheEvents.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(sseEmitter, entry.getKey(), entry.getValue()));
        }

        return sseEmitter;
    }

    private void sendNotification(SseEmitter sseEmitter, String emitterId, Object data) {
        try {
            sseEmitter.send(SseEmitter.event()
                .id(emitterId)
                .name("sse")
                .data(data)
            );
        } catch (IOException e) {
            emitterRepository.deleteById(emitterId);
            throw new NotificationBadRequestException("전송 실패");
        }
    }

    // 알림 전송
    public void send(Member member, NotificationType notificationType, String content) {

        Notification notification = notificationRepository.save(createNotification(member, notificationType, content));

        String memberId = String.valueOf(member.getId());
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithMemberId(memberId);
        emitters.forEach(
            (key, emitter) -> {
                emitterRepository.saveEventCache(key, notification);
                sendNotification(emitter, key, NotificationDto.Response.from(notification));
            }
        );
    }

    private Notification createNotification(Member member, NotificationType notificationType, String content) {
        return Notification.builder()
            .content(content)
            .notificationType(notificationType)
            .member(member)
            .build();
    }
}
