package com.ddang.usedauction.bid.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 포인트 부족으로 인한 입찰 실패 시 메시지 dto
public class BidFailByPointMessageDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private BidStatus bidStatus; // 입찰 성공 유무
        private long previousUsedPoint; // 다른 경매의 현재 회원이 최고입찰가인 경우의 금액의 합
        private long currentPoint; // 현재 회원의 보유 포인트
        private long bidAmount; // 현재 시도한 입찰 금액

        // 실패한 메시지 응답으로 변경하는 메소드
        public static BidFailByPointMessageDto.Response from(long previousUsedPoint,
            long currentPoint, long bidAmount) {

            return BidFailByPointMessageDto.Response.builder()
                .bidStatus(BidStatus.POINT_FAIL)
                .previousUsedPoint(previousUsedPoint)
                .currentPoint(currentPoint)
                .bidAmount(bidAmount)
                .build();
        }
    }
}
