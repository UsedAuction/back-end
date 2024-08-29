package com.ddang.usedauction.mail.exception;

public class MailDeliveryFailedException extends RuntimeException {

    public MailDeliveryFailedException() {

        super("이메일 인증코드 전송 실패");
    }
}
