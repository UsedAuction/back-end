package com.ddang.usedauction.ask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class AskUpdateDto { // 문의 수정 시 dto

    @NotBlank(message = "내용을 입력해주세요.")
    private String content; // 내용
}
