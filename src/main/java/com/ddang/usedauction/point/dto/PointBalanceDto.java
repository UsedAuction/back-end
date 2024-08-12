package com.ddang.usedauction.point.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PointBalanceDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class Response {

        private long pointAmount;

        public static Response from(long pointBalance) {
            return Response.builder()
                .pointAmount(pointBalance)
                .build();
        }
    }
}
