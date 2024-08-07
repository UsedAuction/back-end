package com.ddang.usedauction.config;

import com.ddang.usedauction.member.MemberException;
import com.ddang.usedauction.point.exception.PointException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberException.class)
    private ResponseEntity<GlobalApiResponse<?>> handleMemberException(MemberException e) {
        log.error("MemberException", e);

        return ResponseEntity
            .status(e.getMemberErrorCode().getHttpStatus().value())
            .body(GlobalApiResponse.toGlobalResponseFail(e.getMemberErrorCode().getHttpStatus(), e.getMessage()));
    }

    @ExceptionHandler(PointException.class)
    public ResponseEntity<GlobalApiResponse<?>> pointExceptionHandler(PointException e) {
        log.error("PointException", e);

        return ResponseEntity
            .status(e.getPointErrorCode().getHttpStatus().value())
            .body(GlobalApiResponse.toGlobalResponseFail(e.getPointErrorCode().getHttpStatus(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<?>> exceptionHandler(Exception e) {
        log.error("Exception", e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(GlobalApiResponse.toGlobalResponseFail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}
