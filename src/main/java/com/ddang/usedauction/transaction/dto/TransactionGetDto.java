package com.ddang.usedauction.transaction.dto;

import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.transaction.domain.BuyType;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매 내역 조회시 dto
public class TransactionGetDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response implements Serializable {

        private Long id;
        private Long auctionId; // 경매글 PK
        private String thumbnailUrl; // 대표 이미지
        private String productName; // 상품명
        private double productStatus; // 상품 상태
        private String productColor; // 상품 색상
        private long startPrice; // 시작가
        private long instantPrice; // 즉시 구매가
        private String sellerEmail; // 판매자 이메일
        private String buyerId; // 구매자 아이디
        private Long salePrice; // 판매된 가격
        private TransType transType; // 거래 종료 또는 거래 진행 중
        private BuyType buyType; // 구매 방식 (낙찰 또는 즉시 구매)

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime saleDate; // 판매한 날짜

        public static TransactionGetDto.Response from(Transaction transaction) {

            return Response.builder()
                .id(transaction.getId())
                .productColor(transaction.getAuction().getProductColor())
                .instantPrice(transaction.getAuction().getInstantPrice())
                .thumbnailUrl(
                    transaction.getAuction().getImageList().stream()
                        .filter(i -> i.getImageType().equals(
                            ImageType.THUMBNAIL)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("대표 이미지가 없습니다."))
                        .getImageUrl())
                .productName(transaction.getAuction().getProductName())
                .startPrice(transaction.getAuction().getStartPrice())
                .productStatus(transaction.getAuction().getProductStatus())
                .sellerEmail(transaction.getAuction().getSeller().getEmail())
                .auctionId(transaction.getAuction().getId())
                .saleDate(transaction.getUpdatedAt())
                .salePrice(transaction.getPrice())
                .buyerId(
                    transaction.getBuyer() != null ? transaction.getBuyer().getMemberId() : null)
                .transType(transaction.getTransType())
                .buyType(transaction.getBuyType())
                .build();
        }
    }
}
