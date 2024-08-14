package com.ddang.usedauction.notification.dto;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.notification.domain.Notification;
import com.ddang.usedauction.notification.domain.NotificationType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class NotificationDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Request {

        private Member member;
        private String content;
        private NotificationType notificationType;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Response {

        private Long id;
        private Member member;
        private String content;
        private NotificationType notificationType;
        private LocalDateTime createdAt;

        public static Response from(Notification notification) {
            return Response.builder()
                .id(notification.getId())
                .member(notification.getMember())
                .content(notification.getContent())
                .notificationType(notification.getNotificationType())
                .createdAt(notification.getCreatedAt())
                .build();
        }
    }
}
