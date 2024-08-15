package com.ddang.usedauction.member.exception;

public class DuplicateMemberIdFoundException extends RuntimeException {
    public DuplicateMemberIdFoundException() {
        super("중복된 ID가 존재합니다.");
    }
}

