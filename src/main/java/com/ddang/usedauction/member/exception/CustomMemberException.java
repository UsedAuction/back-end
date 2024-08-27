package com.ddang.usedauction.member.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomMemberException extends RuntimeException {

    private MemberErrorCode errorCode;
    private String message;

    public CustomMemberException(MemberErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
}
