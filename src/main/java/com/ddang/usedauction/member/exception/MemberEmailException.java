package com.ddang.usedauction.member.exception;

public class MemberEmailException extends RuntimeException {

    public MemberEmailException(String provider) {
        super(message(provider));
    }

    private static String message(String provider) {
        if (provider == null || provider.isBlank()) {
            return "일반 로그인으로 가입된 계정이 존재합니다.";
        } else {
            return provider + "에 해당 이메일로 가입된 계정이 존재합니다.";
        }
    }
}
