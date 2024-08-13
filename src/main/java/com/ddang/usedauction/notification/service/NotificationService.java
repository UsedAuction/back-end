package com.ddang.usedauction.notification.service;

import com.ddang.usedauction.notification.exception.NotificationBadRequestException;
import com.ddang.usedauction.notification.repository.EmitterRepository;
import com.ddang.usedauction.notification.repository.NotificationRepository;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
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
    public SseEmitter subscribe(UserDetails userDetails, String lastEventId) {

        String email = userDetails.getUsername(); // TODO: email 대신 id로 수정
        String emitterId = email + "_" + System.currentTimeMillis(); // 유실된 데이터의 시점을 알기 위해 시간을 붙임
        SseEmitter sseEmitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        // 콜백
        sseEmitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        sseEmitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
        sseEmitter.onError((e) -> emitterRepository.deleteById(emitterId));

        // 503 에러방지를 위한 더미 이벤트 전송
        sendNotification(sseEmitter, emitterId, "더미 이벤트 전송 완료 / 회원이메일: " + email);

        // 받지 못한 알림이 있으면 보내주기
        if (!lastEventId.isEmpty()) {
            Map<String, Object> cacheEvents = emitterRepository.findAllEventCacheStartWithEmail(email);
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
}
