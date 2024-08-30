package com.ddang.usedauction.point.dto;

import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.domain.PointType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PointHistoryDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;
        private PointType pointType;
        private long pointAmount;
        private long curPointAmount;
        private Long memberId;

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt;

        public static Response from(PointHistory pointHistory) {
            return Response.builder()
                .id(pointHistory.getId())
                .pointType(pointHistory.getPointType())
                .pointAmount(pointHistory.getPointAmount())
                .curPointAmount(pointHistory.getCurPointAmount())
                .memberId(pointHistory.getMember().getId())
                .createdAt(pointHistory.getCreatedAt())
                .build();
        }
    }
}
