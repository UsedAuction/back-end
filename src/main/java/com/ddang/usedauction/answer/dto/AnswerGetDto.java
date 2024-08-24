package com.ddang.usedauction.answer.dto;

import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.image.dto.ImageGetDto;
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

public class AnswerGetDto {

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
        private List<ImageGetDto.Response> imageList; // 이미지

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt;

        // entity -> getResponse
        public static AnswerGetDto.Response from(Answer answer) {

            return AnswerGetDto.Response.builder()
                .id(answer.getId())
                .auctionId(answer.getAuction().getId())
                .auctionTitle(answer.getAuction().getTitle())
                .title(answer.getTitle())
                .content(answer.getContent())
                .writerId(answer.getAuction().getSeller().getMemberId())
                .imageList(answer.getImageList() != null && !answer.getImageList().isEmpty()
                    ? answer.getImageList().stream().map(ImageGetDto.Response::from).toList()
                    : new ArrayList<>())
                .createdAt(answer.getCreatedAt())
                .build();
        }
    }
}
