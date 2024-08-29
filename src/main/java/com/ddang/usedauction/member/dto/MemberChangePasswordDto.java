package com.ddang.usedauction.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class MemberChangePasswordDto {

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z].*)(?=.*[0-9!@#$%^&*()\\-_=+{};:,<.>].*).{4,16}$"
        , message = "비밀번호는 4자에서 16자 사이로 영문자, 숫자, 특수문자 중 2가지 이상을 조합해야 합니다.")
    private String curPassword;

    @NotBlank(message = "변경할 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z].*)(?=.*[0-9!@#$%^&*()\\-_=+{};:,<.>].*).{4,16}$"
        , message = "비밀번호는 4자에서 16자 사이로 영문자, 숫자, 특수문자 중 2가지 이상을 조합해야 합니다.")
    private String newPassword;
    
    @NotBlank(message = "변경할 비밀번호를 한번 더 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z].*)(?=.*[0-9!@#$%^&*()\\-_=+{};:,<.>].*).{4,16}$"
        , message = "비밀번호는 4자에서 16자 사이로 영문자, 숫자, 특수문자 중 2가지 이상을 조합해야 합니다.")
    private String confirmPassword;
}
