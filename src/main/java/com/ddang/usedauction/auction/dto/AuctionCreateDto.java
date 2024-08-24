package com.ddang.usedauction.auction.dto;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.ReceiveType;
import com.ddang.usedauction.validation.IsEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuctionCreateDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "경매글 제목을 입력해주세요.")
        @Size(max = 255, message = "경매글 제목은 255자까지 입력 가능합니다.")
        private String title; // 경매글 제목

        @IsEnum(message = "올바른 거래 방법을 입력하세요. ex) contact, delivery, all")
        private ReceiveType receiveType; // 거래 방법

        @IsEnum(message = "올바른 택배비 타입을 입력하세요. ex) prepay, noprepay, nodelivery")
        private DeliveryType deliveryType; // 택배비 타입

        @Size(max = 255, message = "거래 장소는 255자까지 입력 가능합니다.")
        private String contactPlace; // 거래 장소

        @PositiveOrZero(message = "입찰 시작가는 음수일 수 없습니다.")
        private long startPrice; // 입찰 시작가

        @PositiveOrZero(message = "즉시 구입가는 음수일 수 없습니다.")
        private long instantPrice; // 즉시 구입가

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime endedAt; // 경매 종료 날짜

        @NotNull(message = "PK값은 null일 수 없습니다.")
        @Positive(message = "PK값은 0 또는 음수일 수 없습니다.")
        private Long parentCategoryId; // 대분류 카테고리 PK

        @NotNull(message = "PK값은 null일 수 없습니다.")
        @Positive(message = "PK값은 0 또는 음수일 수 없습니다.")
        private Long childCategoryId; // 소분류 카테고리 PK

        @NotBlank(message = "상품 이름을 입력해주세요.")
        @Size(max = 255, message = "상품 이름은 255자까지 입력 가능합니다.")
        private String productName; // 상품 이름

        @DecimalMin(value = "0.0", message = "상품 상태는 음수일 수 없습니다.")
        @DecimalMax(value = "5.0", message = "상품 상태의 최대는 5.0입니다.")
        private double productStatus; // 상품 상태

        @Size(max = 255, message = "상품 색상은 255자까지 입력할 수 있습니다.")
        private String productColor; // 상품 색상

        @Size(max = 5000, message = "상품 설명은 5000자까지 입력할 수 있습니다.")
        private String productDescription; // 상품 설명

        @Size(max = 255, message = "택배비는 255자까지 입력할 수 있습니다.")
        private String deliveryPrice; // 택배 거래시 택배비
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private Long id;
        private String title; // 경매글 제목

        // entity -> createResponse
        public static AuctionCreateDto.Response from(Auction auction) {

            return AuctionCreateDto.Response.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .build();
        }
    }
}
