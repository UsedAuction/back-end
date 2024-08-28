package com.ddang.usedauction.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

public class MemberUpdateDto {


    /**
     * 회원정보 수정 Dto
     */

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request{

        @NotBlank(message = "새로운 아이디를 입력해주세요.")
        @Size(min = 6, max = 12, message = "아이디는 6~12자 사이로 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용 가능합니다.")
        private String memberId;

        @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
        @Size(min = 4, max = 16, message = "비밀번호는 4~16자 사이로 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,16}$",
                message = "비밀번호는 4~16자 이내의 숫자, 특수문자, 영문자(대소문자) 중 2가지 이상을 포함해야 합니다."
        )
        private String passWord; // 수정할 비밀번호

        @NotBlank(message = "새로운 비밀번호를 다시 한번 더 입력해주세요.")
        @Size(min = 4, max = 16, message = "비밀번호는 4~16자 사이로 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,16}$",
                message = "비밀번호는 4~16자 이내의 숫자, 특수문자, 영문자(대소문자) 중 2가지 이상을 포함해야 합니다."
        )
        private String confirmNewPassword;

        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        @NotBlank(message = "이메일을 입력해주세요.")
        private String email; // 수정할 이메일
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response{ // 바뀐정보로 넘기는지 확인하기

        private String memberId; // 수정된 아이디
        private String passWord; // 수정된 비밀번호
        private String email; // 수정된 이메일
    }
}
