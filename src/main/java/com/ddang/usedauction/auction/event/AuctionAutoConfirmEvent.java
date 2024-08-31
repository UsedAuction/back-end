package com.ddang.usedauction.auction.event;

import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class AuctionAutoConfirmEvent { // 자동 구매 확정 이벤트

    private Long auctionId;
    private String buyerId;
    private AuctionConfirmDto.Request confirmDto;

    public static AuctionAutoConfirmEvent of(Long auctionId, String buyerId,
        AuctionConfirmDto.Request confirmDto) {

        return AuctionAutoConfirmEvent.builder()
            .auctionId(auctionId)
            .buyerId(buyerId)
            .confirmDto(confirmDto)
            .build();
    }
}
