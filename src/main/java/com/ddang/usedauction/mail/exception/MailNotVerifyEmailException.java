package com.ddang.usedauction.mail.exception;

public class MailNotVerifyEmailException extends RuntimeException {

    public MailNotVerifyEmailException() {

        super("이메일 인증을 완료해주세요.");
    }
}
