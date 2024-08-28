package com.ddang.usedauction.member.dto;

import com.ddang.usedauction.member.domain.enums.Role;
import com.ddang.usedauction.validation.IsEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 Dto
 */

public class MemberSignupDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(min = 6, max = 12)
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문자와 숫자만 사용 가능합니다.")
        private String memberId;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 4, max = 16, message = "비밀번호는 4~16자 사이로 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,16}$",
                message = "비밀번호는 4~16자 이내의 숫자, 특수문자, 영문자(대소문자) 중 2가지 이상을 포함해야 합니다."
        )
        private String passWord;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 4, max = 16, message = "비밀번호는 4~16자 사이로 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,16}$",
                message = "비밀번호는 4~16자 이내의 숫자, 특수문자, 영문자(대소문자) 중 2가지 이상을 포함해야 합니다."
        )
        private String confirmPassWord;

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "유효한 이메일 주소를 입력해주세요.")
        @Pattern(regexp = "[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
                message = "유효한 이메일 주소를 입력해주세요.")

        private String email;

//        @NotBlank(message = "인증코드를 입력해주세요.")
//        @Pattern(regexp = "\\d{4}", message = "인증코드는 4자리 숫자입니다.")
//        private String AuthCode; // 인증코드번호
    }
}
