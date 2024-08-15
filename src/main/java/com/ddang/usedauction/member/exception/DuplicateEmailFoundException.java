package com.ddang.usedauction.member.exception;

public class DuplicateEmailFoundException extends RuntimeException {
    public DuplicateEmailFoundException() {
        super("중복된 이메일이 존재합니다.");
    }
}
