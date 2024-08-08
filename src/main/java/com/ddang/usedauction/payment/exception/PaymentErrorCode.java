package com.ddang.usedauction.payment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode {

    INVALID_USER(HttpStatus.BAD_REQUEST.value(), "동일한 유저가 아닙니다."),
    PAYMENT_CANCEL(HttpStatus.BAD_REQUEST.value(), "결제가 취소되었습니다."),
    PAYMENT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR.value(), "결제에 실패하였습니다.")
    ;

    private final int status;
    private final String message;
}
