package com.ddang.usedauction.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    DONE("DONE", "경매 종료"),
    DONE_INSTANT("DONE_INSTANT", "즉시 구매로 인한 경매 종료"),
    QUESTION("QUESTION", "문의 질문 알림"),
    ANSWER("ANSWER", "문의 답변 알림"),
    CONFIRM("CONFIRM", "구매 확정 알림")
    ;

    private final String name;
    private final String description;
}
