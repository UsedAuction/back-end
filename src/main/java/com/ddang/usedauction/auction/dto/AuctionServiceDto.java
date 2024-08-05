package com.ddang.usedauction.auction.dto;

import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class AuctionServiceDto implements Serializable {

    private Long id;
    private String title; // 경매 제목
    private AuctionState auctionState; // 경매 상태
    private String productName; // 상품 이름
    private String productColor; // 상품 색상
    private double productStatus; // 상품 상태
    private String productDescription; // 상품 설명
    private boolean contact; // 대면 거래 가능 여부
    private boolean delivery; // 택배 거래 가능 여부
    private DeliveryType deliveryType; // 택배비 타입
    private String deliveryPrice; // 택배비
    private long currentPrice; // 현재 입찰가
    private long startPrice; // 입찰 시작가
    private long instantPrice; // 즉시 구매가

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime endedAt; // 경매 마감일

    //todo : 회원 및 카테고리 작업 완료 후 dto로 저장
}
