package com.ddang.usedauction.member.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String s) {
        super("등록되지 않은 아이디입니다.");
    }
}

