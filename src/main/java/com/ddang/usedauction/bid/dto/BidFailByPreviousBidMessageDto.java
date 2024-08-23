package com.ddang.usedauction.bid.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 먼저 입찰한 회원이 있어 입찰 실패 시 메시지 dto
public class BidFailByPreviousBidMessageDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private BidStatus bidStatus; // 입찰 성공 유무
        private long previousBidAmount; // 이전 입찰가
        private long currentBidAmount; // 현재 입찰 시도한 금액

        // 실패한 메시지 응답으로 변경하는 메소드
        public static BidFailByPreviousBidMessageDto.Response from(long previousBidAmount,
            long currentBidAmount) {

            return Response.builder()
                .bidStatus(BidStatus.BID_FAIL)
                .previousBidAmount(previousBidAmount)
                .currentBidAmount(currentBidAmount)
                .build();
        }
    }
}
