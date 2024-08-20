package com.ddang.usedauction.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {
    NOT_AUTH_OF_MAIL(HttpStatus.BAD_REQUEST.value(), "이메일 인증을 진행하지 않았습니다."),
    ALREADY_LOGOUT(HttpStatus.BAD_REQUEST.value(), "이미 로그아웃된 유저입니다."),
    NOT_MATCH_CODE(HttpStatus.BAD_REQUEST.value(), "인증코드가 올바르게 입력되지 않았습니다."),
    NOT_MATCH_PASSWORD(HttpStatus.BAD_REQUEST.value(), "비밀번호가 틀립니다."),
    EXIST_USER_ID(HttpStatus.BAD_REQUEST.value(), "이미 사용중인 아이디입니다"),
    EXIST_EMAIL(HttpStatus.BAD_REQUEST.value(), "이미 사용중인 이메일입니다"),
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST.value(), "회원으로 등록된 유저가 아닙니다.");

    private final int status;
    private final String message;
}