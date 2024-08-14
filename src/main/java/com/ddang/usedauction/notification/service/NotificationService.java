package com.ddang.usedauction.notification.service;

import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.dto.NotificationDto;
import com.ddang.usedauction.notification.exception.NotificationBadRequestException;
import com.ddang.usedauction.notification.repository.EmitterRepository;
import com.ddang.usedauction.notification.repository.NotificationRepository;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${notification.timeout}")
    private Long DEFAULT_TIMEOUT;

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
        sendNotification(sseEmitter, emitterId, "연결 완료 / memberId: " + memberId);

        // 받지 못한 알림이 있으면 보내주기
        if (!lastEventId.isEmpty()) {
            Map<String, Object> cacheEvents =
                emitterRepository.findAllEventCacheStartWithMemberId(String.valueOf(memberId));
            cacheEvents.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(sseEmitter, entry.getKey(), entry.getValue()));
        }

        return sseEmitter;
    }

    // 알림 전송
    @Transactional
    public void send(NotificationDto.Request request) {

        Notification notification = notificationRepository.save(createNotification(request));

        String memberId = String.valueOf(request.getMember().getId());
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithMemberId(memberId);
        emitters.forEach(
            (key, emitter) -> {
                emitterRepository.saveEventCache(key, notification);
                sendNotification(emitter, key, NotificationDto.Response.from(notification));
            }
        );
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

    private Notification createNotification(NotificationDto.Request request) {
        return Notification.builder()
            .member(request.getMember())
            .content(request.getContent())
            .notificationType(request.getNotificationType())
            .build();
    }
}
