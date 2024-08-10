package com.ddang.usedauction.auction.dto;

import com.ddang.usedauction.ask.dto.AskGetDto;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.TransactionType;
import com.ddang.usedauction.bid.dto.BidGetDto;
import com.ddang.usedauction.category.dto.CategoryServiceDto;
import com.ddang.usedauction.image.dto.ImageGetDto;
import com.ddang.usedauction.member.dto.MemberServiceDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuctionGetDto {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder(toBuilder = true)
    public static class Response {

        private Long id;
        private String title; // 경매 제목
        private AuctionState auctionState; // 경매 상태
        private String productName; // 상품 이름
        private String productColor; // 상품 색상
        private double productStatus; // 상품 상태
        private String productDescription; // 상품 설명
        private TransactionType transactionType; // 거래 방식
        private String contactPlace; // 대면 거래 장소
        private DeliveryType deliveryType; // 택배비 타입
        private String deliveryPrice; // 택배비
        private long currentPrice; // 현재 입찰가
        private long startPrice; // 입찰 시작가
        private long instantPrice; // 즉시 구매가
        private long memberCount; // 경매에 참여한 회원 수

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime endedAt; // 경매 마감일

        private MemberServiceDto seller; // 판매자
        private CategoryServiceDto parentCategory; // 대분류 카테고리
        private CategoryServiceDto childCategory; // 소분류 카테고리
        private List<BidGetDto.Response> bidList; // 입찰 리스트
        private List<AskGetDto.Response> askList; // 문의글 리스트
        private List<ImageGetDto.Response> imageList; // 이미지 리스트

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt; // 생성 날짜
    }
}
