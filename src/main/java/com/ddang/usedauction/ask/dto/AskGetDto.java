package com.ddang.usedauction.ask.dto;

import com.ddang.usedauction.answer.dto.AnswerGetDto;
import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.member.dto.MemberGetDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class AskGetDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private Long id;
        private String title;
        private String content;
        private MemberGetDto.Response writer;
        private List<AnswerGetDto.Response> answerList;
        private LocalDateTime createdAt;

        // entity -> getResponse
        public static AskGetDto.Response from(Ask ask) {

            return AskGetDto.Response.builder()
                .id(ask.getId())
                .title(ask.getTitle())
                .content(ask.getContent())
                .writer(MemberGetDto.Response.from(ask.getWriter()))
                .answerList(ask.getAnswerList() != null && !ask.getAnswerList().isEmpty()
                    ? ask.getAnswerList().stream().map(AnswerGetDto.Response::from).toList()
                    : new ArrayList<>())
                .createdAt(ask.getCreatedAt())
                .build();
        }
    }
}
