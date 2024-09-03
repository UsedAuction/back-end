package com.ddang.usedauction.notification.service;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.domain.NotificationType;
import com.ddang.usedauction.notification.dto.NotificationDto;
import com.ddang.usedauction.notification.dto.NotificationDummyDto;
import com.ddang.usedauction.notification.repository.EmitterRepository;
import com.ddang.usedauction.notification.repository.NotificationRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
    private final AuctionRepository auctionRepository;

    // 알림 구독
    public SseEmitter subscribe(String memberId, String lastEventId) {

        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        String emitterId =
            member.getId() + "_" + System.currentTimeMillis(); // 유실된 데이터의 시점을 알기 위해 시간을 붙임
        SseEmitter sseEmitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        // 콜백
        sseEmitter.onCompletion(() -> {
            log.info("onCompletion emitterId: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        sseEmitter.onTimeout(() -> {
            log.info("onTimeout emitterId: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });
        sseEmitter.onError((e) -> {
                log.info("onError emitterId: {}", emitterId);
            emitterRepository.deleteById(emitterId);
        });

        String dummyStr = "연결 완료 / memberId: " + member.getId();

        // 503 에러방지를 위한 더미 이벤트 전송
        sendNotification(sseEmitter, emitterId, NotificationDummyDto.builder().dummyContent(dummyStr).build());

        // 받지 못한 알림이 있으면 보내주기
        if (!lastEventId.isEmpty()) {
            Map<String, Object> cacheEvents =
                emitterRepository.findAllEventCacheStartWithMemberId(String.valueOf(member.getId()));
            cacheEvents.entrySet().stream()
                .filter(eventCache -> lastEventId.compareTo(eventCache.getKey()) < 0)
                .forEach(eventCache -> sendNotification(sseEmitter, eventCache.getKey(), eventCache.getValue()));
        }

        return sseEmitter;
    }

    // 알림 전송
    @Transactional
    @Async
    public void send(Long memberId, Long auctionId, String content,
        NotificationType notificationType) {

        log.info("알림 전송 send()");

        Notification notification =
            notificationRepository.save(createNotification(memberId, auctionId, content, notificationType));
        log.info("notification: {}", notification);

        Map<String, SseEmitter> emitters =
            emitterRepository.findAllEmitterStartWithMemberId(String.valueOf(memberId));
        log.info("emitters: {}", emitters.keySet());

        emitters.forEach(
            (emitterId, emitter) -> {
                log.info("emitterId: {}, emitter: {}", emitterId, emitter.toString());
                emitterRepository.saveEventCache(emitterId, notification);
                sendNotification(emitter, emitterId, NotificationDto.Response.from(notification));
            }
        );
    }

    // 알림 전체 목록 조회
    @Transactional(readOnly = true)
    public List<Notification> getNotificationList(String memberId) {

        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        LocalDateTime beforeOneMonth = LocalDateTime.now().minusMonths(1);

        return notificationRepository.findNotificationList(memberId, beforeOneMonth);
    }

    // 실제로 알림을 전송하는 메서드
    private void sendNotification(SseEmitter sseEmitter, String emitterId, Object data) {
        try {
            log.info("emitterId: {}", emitterId);
            log.info("data: {}", data.toString());

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

    // 알림 생성 메서드
    private Notification createNotification(Long memberId, Long auctionId, String content,
        NotificationType notificationType) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        return Notification.builder()
            .member(member)
            .auctionId(auction.getId())
            .content(content)
            .notificationType(notificationType)
            .build();
    }
}
