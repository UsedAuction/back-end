package com.ddang.usedauction.ask.dto;

import com.ddang.usedauction.answer.dto.AnswerGetDto;
import com.ddang.usedauction.member.domain.Member;
import java.time.LocalDateTime;
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
        private Member writer;
        private List<AnswerGetDto.Response> answerList;
        private LocalDateTime deletedAt;
    }
}
