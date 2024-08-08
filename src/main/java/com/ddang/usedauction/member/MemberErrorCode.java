package com.ddang.usedauction.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
