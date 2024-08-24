package com.ddang.usedauction.answer.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class AnswerUpdateDto { // 답변 수정 시 dto

    @NotBlank(message = "수정할 내용을 입력해주세요.")
    private String content; // 내용

    private List<@NotBlank(message = "삭제할 이미지 파일 이름을 입력해주세요.") String> imageFileNameList; // 삭제할 이미지
}
