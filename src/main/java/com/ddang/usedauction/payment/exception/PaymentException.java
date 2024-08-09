package com.ddang.usedauction.payment.exception;

import lombok.Getter;

@Getter
public class PaymentException extends RuntimeException {

    private final PaymentErrorCode paymentErrorCode;

    public PaymentException(PaymentErrorCode paymentErrorCode) {
        super(paymentErrorCode.getMessage());
        this.paymentErrorCode = paymentErrorCode;
    }
}
