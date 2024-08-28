package com.ddang.usedauction.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class MailUpdateDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request{
        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        @NotBlank(message = "이메일을 입력해주세요.")
        private String email; // 수정할 이메일
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response{
        private String email; // 수정된 이메일
    }
}
