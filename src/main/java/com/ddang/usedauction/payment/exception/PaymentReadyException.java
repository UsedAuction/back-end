package com.ddang.usedauction.payment.exception;

import lombok.Getter;

@Getter
public class PaymentReadyException extends RuntimeException {

    public PaymentReadyException(String message) {
        super(message);
    }
}
