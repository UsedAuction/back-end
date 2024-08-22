package com.ddang.usedauction.ask.dto;

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

    private String title; // 제목
    private String content; // 내용
    private Long auctionId; // 경매 pk
}
