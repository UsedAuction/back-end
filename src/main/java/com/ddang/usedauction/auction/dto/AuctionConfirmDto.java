package com.ddang.usedauction.auction.dto;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuctionConfirmDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request {

        @Positive(message = "거래 가격은 0 또는 음수일 수 없습니다.")
        private long price; // 거래 가격

        @Positive(message = "PK값은 0 또는 음수일 수 없습니다.")
        private Long sellerId; // 판매자 PK
    }
}
