package com.ddang.usedauction.member.exception;

public class IllegalMemberAccessException extends RuntimeException {
    public IllegalMemberAccessException() {
        super("수정 및 삭제 권한이 없습니다.");
    }

    public IllegalMemberAccessException(String message) {
        super(message);
    }
}

