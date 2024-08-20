package com.ddang.usedauction.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 Dto
 */

public class MemberLoginDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request{

        @NotBlank(message = "아이디를 입력해주세요.")
        private String memberId;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String passWord;
    }
}