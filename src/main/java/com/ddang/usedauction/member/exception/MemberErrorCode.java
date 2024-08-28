package com.ddang.usedauction.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {
    NOT_AUTH_OF_MAIL(HttpStatus.BAD_REQUEST, "이메일 인증을 진행하지 않았습니다."),
    ALREADY_LOGOUT(HttpStatus.BAD_REQUEST, "이미 로그아웃된 유저입니다."),
    NOT_MATCH_CODE(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다."),
    NOT_MATCH_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    NOT_MATCH_NEWPASSWORD(HttpStatus.BAD_REQUEST, "새로운 비밀번호가 틀립니다."),
    EXIST_USER_ID(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    EXIST_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 이메일입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."),
    EMAIL_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "이메일 인증에 실패했습니다."),
    MEMBER_ID_EQUALS_EMAIL(HttpStatus.BAD_REQUEST,"아이디와 이메일이 일치합니다.");

    private final HttpStatus status;
    private final String message;
}
