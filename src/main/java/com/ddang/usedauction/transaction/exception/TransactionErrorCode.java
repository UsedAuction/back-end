package com.ddang.usedauction.transaction.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TransactionErrorCode {

    NOT_FOUND_TRANSACTION(HttpStatus.BAD_REQUEST.value(), "등록되지 않은 거래입니다.");

    private final int status;
    private final String message;
}
