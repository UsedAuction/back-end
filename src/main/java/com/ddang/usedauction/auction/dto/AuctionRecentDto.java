package com.ddang.usedauction.auction.dto;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.image.domain.ImageType;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
// 최근 본 경매 dto
public class AuctionRecentDto implements Serializable {

    private Long id;
    private String auctionTitle;
    private String imageUrl;

    // AuctionRecentDto로 변경하는 메소드
    public static AuctionRecentDto from(Auction auction) {

        return AuctionRecentDto.builder()
            .id(auction.getId())
            .auctionTitle(auction.getTitle())
            .imageUrl(auction.getImageList().stream()
                .filter(i -> i.getImageType().equals(ImageType.THUMBNAIL)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("대표이미지가 없습니다.")).getImageUrl())
            .build();
    }
}
