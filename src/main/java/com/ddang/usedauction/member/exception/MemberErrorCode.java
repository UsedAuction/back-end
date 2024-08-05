package com.ddang.usedauction.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {

    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST.value(), "등록되지 않은 회원입니다.");

    private final int status;
    private final String message;
}
