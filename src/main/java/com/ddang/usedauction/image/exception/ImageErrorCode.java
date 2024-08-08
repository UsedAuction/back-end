package com.ddang.usedauction.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode {

    FAIL_UPLOAD_IMAGE(HttpStatus.BAD_REQUEST.value(), "이미지를 s3로 업로드하는데 실패하였습니다."),
    FAIL_DELETE_IMAGE(HttpStatus.BAD_REQUEST.value(), "이미지를 s3에서 삭제하는데 실패하였습니다."),
    NOT_FOUND_IMAGE(HttpStatus.BAD_REQUEST.value(), "등록되지 않은 이미지입니다.");

    private final int status;
    private final String message;
}
