package com.ddang.usedauction.notification.controller;

import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.dto.NotificationDto;
import com.ddang.usedauction.notification.dto.NotificationDto.Response;
import com.ddang.usedauction.notification.service.NotificationService;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * @Param memberId 회원 pk
     * @param lastEventId 마지막 이벤트 id
     * @return 성공 시 200 코드와 sseEmitter, 실패 시 에러메시지
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
        @RequestParam String memberId,
        @RequestParam(required = false, defaultValue = "") String lastEventId
    ) {
        return ResponseEntity.ok(notificationService.subscribe(memberId, lastEventId));
    }

    /**
     * 알림 전체 목록 조회
     *
     * @return 성공 시 200 코드와 알림 전체 목록, 실패 시 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<NotificationDto.Response>> getNotificationList(
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        String memberId = principalDetails.getName();
        List<Notification> notifications = notificationService.getNotificationList(memberId);
        return ResponseEntity.ok(notifications.stream().map(Response::from).toList());
    }
}
