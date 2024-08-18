package com.ddang.usedauction.auction.dto;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.bid.domain.Bid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 경매 종료 시 dto
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class AuctionEndDto {

    private Long auctionId; // 경매 pk
    private Long buyerId; // 구매자 pk, 없으면 null
    private Long sellerId; // 판매자 pk
    private long price; // 판매 가격

    // dto로 변경해주는 메소드
    public static AuctionEndDto from(Auction auction, Bid bid) {

        return AuctionEndDto.builder()
            .auctionId(auction.getId())
            .buyerId(bid != null ? bid.getMember().getId() : null)
            .sellerId(auction.getSeller().getId())
            .price(bid != null ? bid.getBidPrice() : 0)
            .build();
    }
}
