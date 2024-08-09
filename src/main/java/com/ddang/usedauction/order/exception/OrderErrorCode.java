package com.ddang.usedauction.order.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OrderErrorCode {

    NOT_FOUND_ORDER(HttpStatus.BAD_REQUEST.value(), "주문내역이 존재하지 않습니다.")
    ;

    private final int status;
    private final String message;
}
