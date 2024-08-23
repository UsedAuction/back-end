package com.ddang.usedauction.ask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class AskCreateDto { // 문의글 생성 시 dto

    @NotBlank(message = "제목을 입력해주세요.")
    private String title; // 제목

    @NotBlank(message = "내용을 입력해주세요.")
    private String content; // 내용

    @NotNull(message = "pk 값은 null 일 수 없습니다.")
    @Positive(message = "pk 값은 0 또는 음수일 수 없습니다.")
    private Long auctionId; // 경매 pk
}
