package com.ddang.usedauction.member.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode {

    NOT_MATCHED_PASSWORD("패스워드가 일치하지 않습니다."),
    NOT_MATCHED_CHECK_PASSWORD("입력한 비밀번호가 서로 다릅니다. 다시 확인해주세요."),
    DUPLICATED_EMAIL("동일한 이메일로 변경할 수 없습니다."),
    ALREADY_EXISTS_MEMBER_ID("이미 존재하는 아이디입니다."),
    ALREADY_EXISTS_EMAIL("이미 존재하는 이메일입니다.");

    private final String message;
}
