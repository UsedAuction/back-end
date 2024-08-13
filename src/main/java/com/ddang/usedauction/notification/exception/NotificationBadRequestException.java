package com.ddang.usedauction.notification.exception;

import lombok.Getter;

@Getter
public class NotificationBadRequestException extends RuntimeException {

    public NotificationBadRequestException(String message) {
        super(message);
    }
}
