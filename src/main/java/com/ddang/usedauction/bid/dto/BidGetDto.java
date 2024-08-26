package com.ddang.usedauction.bid.dto;

import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.image.domain.ImageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BidGetDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private Long id;
        private long bidPrice; // 입찰가
        private Long auctionId; // 입찰한 경매 PK
        private String auctionTitle; // 경매 제목
        private String thumbnailImageUrl; // 대표이미지 url
        private String memberId; // 입찰한 회원 id

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt; // 생성 날짜

        // entity -> getResponse
        public static BidGetDto.Response from(Bid bid) {

            return BidGetDto.Response.builder()
                .id(bid.getId())
                .bidPrice(bid.getBidPrice())
                .auctionId(bid.getAuction().getId())
                .auctionTitle(bid.getAuction().getTitle())
                .thumbnailImageUrl(
                    bid.getAuction().getImageList().stream().filter(i -> i.getImageType().equals(
                            ImageType.THUMBNAIL)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("대표 이미지가 없습니다."))
                        .getImageUrl())
                .memberId(bid.getMember().getMemberId())
                .createdAt(bid.getCreatedAt())
                .build();
        }
    }
}
