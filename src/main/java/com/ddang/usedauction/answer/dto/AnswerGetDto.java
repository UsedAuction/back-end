package com.ddang.usedauction.answer.dto;

import com.ddang.usedauction.answer.domain.Answer;
import java.time.LocalDateTime;
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
        private String content;
        private LocalDateTime createdAt;

        // entity -> getResponse
        public static AnswerGetDto.Response from(Answer answer) {

            return AnswerGetDto.Response.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .build();
        }
    }
}
