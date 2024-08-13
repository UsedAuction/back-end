package com.ddang.usedauction.payment.exception;

import lombok.Getter;

@Getter
public class PaymentApproveException extends RuntimeException {

    public PaymentApproveException(String message) {
        super(message);
    }
}
