package com.ddang.usedauction.auction.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class AuctionEndEvent { // 경매 종료 이벤트

    private Long auctionId;

    public static AuctionEndEvent of(Long auctionId) {

        return AuctionEndEvent.builder()
            .auctionId(auctionId)
            .build();
    }
}
