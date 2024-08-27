package com.ddang.usedauction.mail.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CustomMailException extends RuntimeException {

    private EmailErrorCode emailErrorCode;
    private String errorMessage;

    public CustomMailException(EmailErrorCode emailErrorCode) {
        this.emailErrorCode = emailErrorCode;
        this.errorMessage = emailErrorCode.getMessage();
    }
}
