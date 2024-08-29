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
public class MailException extends RuntimeException {

    private MailErrorCode mailErrorCode;
    private String errorMessage;

    public MailException(MailErrorCode mailErrorCode) {
        this.mailErrorCode = mailErrorCode;
        this.errorMessage = mailErrorCode.getMessage();
    }
}
