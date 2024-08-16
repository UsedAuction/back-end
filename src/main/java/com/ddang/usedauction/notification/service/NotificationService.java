package com.ddang.usedauction.notification.service;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.domain.NotificationType;
import com.ddang.usedauction.notification.dto.NotificationDto;
import com.ddang.usedauction.notification.repository.EmitterRepository;
import com.ddang.usedauction.notification.repository.NotificationRepository;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final MemberRepository memberRepository;

    /**
     * 알림 구독
     *
     * @param memberId 회원 id (PK)
     * @param lastEventId 마지막 이벤트 id
     * @return SseEmitter
     */
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

    /**
     * 알림 전송
     *
     * @param memberId 회원 id (PK)
     * @param content 알림 내용
     * @param notificationType 알림 타입
     */
    @Transactional
    public void send(Long memberId, String content, NotificationType notificationType) {

        Notification notification =
            notificationRepository.save(createNotification(memberId, content, notificationType));

        Map<String, SseEmitter> emitters =
            emitterRepository.findAllEmitterStartWithMemberId(String.valueOf(memberId));

        emitters.forEach(
            (key, emitter) -> {
                emitterRepository.saveEventCache(key, notification);
                sendNotification(emitter, key, NotificationDto.Response.from(notification));
            }
        );
    }

    /**
     * 알림 전체 목록 조회
     *
     * @param pageable
     * @return Page<Notification>
     */
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationList(Pageable pageable) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private void sendNotification(SseEmitter sseEmitter, String emitterId, Object data) {
        try {
            sseEmitter.send(SseEmitter.event()
                .id(emitterId)
                .name("sse")
                .data(data)
            );
        } catch (IOException e) {
            log.error("전송 실패 - emitterId: {}, error: {}", emitterId, e.getMessage());
            emitterRepository.deleteById(emitterId);
        }
    }

    private Notification createNotification(Long memberId, String content, NotificationType notificationType) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return Notification.builder()
            .member(member)
            .content(content)
            .notificationType(notificationType)
            .build();
    }
}
