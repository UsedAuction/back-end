package com.ddang.usedauction.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode {

    NOT_FOUND_IMAGE(HttpStatus.BAD_REQUEST.value(), "등록되지 않은 이미지입니다.");

    private final int status;
    private final String message;
}
