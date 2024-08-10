package com.ddang.usedauction.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 전체 api 응답 형식 통일화
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class FailedApiResponse {

    private String message; // 응답 관련 메시지

    // 응답 실패 시
    public static FailedApiResponse toFailedResponse(String message) {

        return FailedApiResponse.builder()
            .message(message)
            .build();
    }
}