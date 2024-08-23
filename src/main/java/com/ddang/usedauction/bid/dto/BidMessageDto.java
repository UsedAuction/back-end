package com.ddang.usedauction.bid.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 입찰 및 입찰 성공 시 메시지 dto
public class BidMessageDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request {

        private Long auctionId; // 경매 pk
        private String memberId; // 회원 아이디
        private long bidAmount; // 입찰 금액
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private BidStatus bidStatus; // 입찰 성공 유무
        private Long auctionId; // 경매 pk
        private String memberId; // 회원 아이디
        private long bidAmount; // 입찰 금액

        // bidRequest -> bidResponse
        public static BidMessageDto.Response from(BidMessageDto.Request bidRequest,
            String memberId) {

            return Response.builder()
                .bidStatus(BidStatus.SUCCESS)
                .auctionId(bidRequest.getAuctionId())
                .memberId(memberId)
                .bidAmount(bidRequest.getBidAmount())
                .build();
        }
    }
}
