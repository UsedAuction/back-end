package com.ddang.usedauction.member.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인증코드 확인용 dto
public class VerifyCodeDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "올바른 이메일을 입력해주세요.")
        private String email; // 이메일

        @NotBlank(message = "인증코드를 입력해주세요.")
        @Pattern(regexp = "\\d{4}", message = "인증코드는 4자리 숫자입니다.")
        private String code; // 입력한 코드 번호
    }
}