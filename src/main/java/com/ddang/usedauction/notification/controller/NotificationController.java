package com.ddang.usedauction.notification.controller;

import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.dto.NotificationDto;
import com.ddang.usedauction.notification.dto.NotificationDto.Response;
import com.ddang.usedauction.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/notification")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 구독
     *
     * @param lastEventId 마지막 이벤트 id
     * @return 성공 시 200 코드와 sseEmitter, 실패 시 에러메시지
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
        @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId
    ) {
        long memberId = 1L; // TODO: 토큰 사용으로 수정
        return ResponseEntity.ok(notificationService.subscribe(memberId, lastEventId));
    }

    /**
     * 알림 전체 목록 조회
     *
     * @param pageable
     * @return 성공 시 200 코드와 알림 전체 목록, 실패 시 에러메시지
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDto.Response>> getNotificationList(@PageableDefault Pageable pageable) {

        Page<Notification> notificationPage = notificationService.getNotificationList(pageable);
        return ResponseEntity.ok(notificationPage.map(Response::from));
    }
}
