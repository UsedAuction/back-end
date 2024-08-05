package com.ddang.usedauction.category.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode {

    NOT_FOUND_CATEGORY(HttpStatus.BAD_REQUEST.value(), "등록되지 않은 카테고리입니다.");

    private final int status;
    private final String message;
}
