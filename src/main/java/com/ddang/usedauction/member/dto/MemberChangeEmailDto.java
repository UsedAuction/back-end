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
public class MemberChangeEmailDto {

    @NotBlank(message = "변경할 이메일을 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
        message = "유효한 이메일 주소를 입력해주세요.")
    private String email;

    @NotBlank(message = "인증 번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{4}$", message = "인증 번호는 4자리 숫자여야 합니다.")
    private String authNum;
}
