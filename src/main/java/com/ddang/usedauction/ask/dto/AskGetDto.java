package com.ddang.usedauction.ask.dto;

import com.ddang.usedauction.answer.dto.AnswerGetDto;
import com.ddang.usedauction.ask.domain.Ask;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AskGetDto { // 질문 조회 시 dto

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private Long id;
        private Long auctionId; // 경매 pk
        private String auctionTitle; // 경매 제목
        private String title; // 제목
        private String content; // 내용
        private String writerId; // 작성자 아이디
        private List<AnswerGetDto.Response> answerList; // 답변 리스트

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt;

        // entity -> getResponse
        public static AskGetDto.Response from(Ask ask) {

            return AskGetDto.Response.builder()
                .id(ask.getId())
                .auctionId(ask.getAuction().getId())
                .auctionTitle(ask.getAuction().getTitle())
                .title(ask.getTitle())
                .content(ask.getContent())
                .writerId(ask.getWriter().getDeletedAt() == null ? ask.getWriter().getMemberId()
                    : "탈퇴한 회원")
                .answerList(ask.getAnswerList() != null && !ask.getAnswerList().isEmpty()
                    ? ask.getAnswerList().stream().map(AnswerGetDto.Response::from).toList()
                    : new ArrayList<>())
                .createdAt(ask.getCreatedAt())
                .build();
        }
    }
}
