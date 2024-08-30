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
public class AuctionGetForChatDto implements Serializable {

    private String title;
    private String thumbnail;

    public static AuctionGetForChatDto from(Auction auction) {
        return AuctionGetForChatDto.builder()
            .title(auction.getTitle())
            .thumbnail(auction.getImageList().stream()
                .filter(thumb -> thumb.getImageType() == ImageType.THUMBNAIL)
                .map(thumb -> thumb.getImageUrl())
                .findFirst().orElse(null))
            .build();
    }
}
