package com.ddang.usedauction.payment.exception;

import lombok.Getter;

@Getter
public class PaymentRequestTimeoutException extends RuntimeException {

    public PaymentRequestTimeoutException(String message) {
        super(message);
    }
}
