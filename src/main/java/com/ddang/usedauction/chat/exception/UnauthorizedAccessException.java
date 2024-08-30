package com.ddang.usedauction.chat.exception;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException() {
        super("채팅방에 속한 회원이 아닙니다.");
    }

}
