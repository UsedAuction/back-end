package com.ddang.usedauction.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

public class UpdatePasswordDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        private String passWord; // 현재 비밀번호

        @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
        @Size(min = 4, max = 16, message = "비밀번호는 4~16자 사이로 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,16}$",
                message = "비밀번호는 4~16자 이내의 숫자, 특수문자, 영문자(대소문자) 중 2가지 이상을 포함해야 합니다."
        )
        private String newPassWord; // 수정할 비밀번호

        @NotBlank(message = "새로운 비밀번호를 다시 한번 더 입력해주세요.")
        @Size(min = 4, max = 16, message = "비밀번호는 4~16자 사이로 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,16}$",
                message = "비밀번호는 4~16자 이내의 숫자, 특수문자, 영문자(대소문자) 중 2가지 이상을 포함해야 합니다."
        )
        private String confirmNewPassword;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {
        private String passWord; // 수정된 비밀번호
    }
}
