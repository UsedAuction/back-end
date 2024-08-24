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
    private String buyerEmail;
    private AuctionConfirmDto.Request confirmDto;

    public static AuctionAutoConfirmEvent of(Long auctionId, String buyerEmail,
        AuctionConfirmDto.Request confirmDto) {

        return AuctionAutoConfirmEvent.builder()
            .auctionId(auctionId)
            .buyerEmail(buyerEmail)
            .confirmDto(confirmDto)
            .build();
    }
}
