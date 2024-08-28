package com.ddang.usedauction.member.exception;

import lombok.Getter;

@Getter
public class MailSendException extends RuntimeException {
    public MailSendException() {
        super("중복된 ID가 존재합니다.");
    }
}

