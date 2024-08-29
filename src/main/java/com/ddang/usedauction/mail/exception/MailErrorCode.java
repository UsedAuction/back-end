package com.ddang.usedauction.mail.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailErrorCode {

    NOT_MATCH_AUTH("인증코드가 일치하지 않습니다."),
    ALREADY_VERIFY("이미 인증이 완료되었습니다."),
    EXPIRE_CODE("인증 시간이 만료되었습니다.");
    private final String message;
}
