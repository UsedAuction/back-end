package com.ddang.usedauction.auction.dto;

import com.ddang.usedauction.ask.dto.AskGetDto;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.ReceiveType;
import com.ddang.usedauction.bid.dto.BidGetDto;
import com.ddang.usedauction.category.dto.CategoryGetDto;
import com.ddang.usedauction.image.dto.ImageGetDto;
import com.ddang.usedauction.member.dto.MemberGetDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public static class Response implements Serializable {

        private Long id;
        private String title; // 경매 제목
        private AuctionState auctionState; // 경매 상태
        private String productName; // 상품 이름
        private String productColor; // 상품 색상
        private double productStatus; // 상품 상태
        private String productDescription; // 상품 설명
        private ReceiveType receiveType; // 거래 방식
        private String contactPlace; // 대면 거래 장소
        private DeliveryType deliveryType; // 택배비 타입
        private String deliveryPrice; // 택배비
        private long currentPrice; // 현재 입찰가
        private long startPrice; // 입찰 시작가
        private long instantPrice; // 즉시 구매가
        private long memberCount; // 경매에 참여한 회원 수

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime endedAt; // 경매 마감일

        private MemberGetDto.Response seller; // 판매자
        private CategoryGetDto.Response parentCategory; // 대분류 카테고리
        private CategoryGetDto.Response childCategory; // 소분류 카테고리
        private List<BidGetDto.Response> bidList; // 입찰 리스트
        private List<AskGetDto.Response> askList; // 문의글 리스트
        private List<ImageGetDto.Response> imageList; // 이미지 리스트

        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/seoul")
        private LocalDateTime createdAt; // 생성 날짜

        // entity -> getResponse
        public static AuctionGetDto.Response from(Auction auction) {

            return Response.builder()
                .id(auction.getId())
                .title(auction.getTitle())
                .auctionState(auction.getAuctionState())
                .productName(auction.getProductName())
                .productColor(auction.getProductColor())
                .productStatus(auction.getProductStatus())
                .productDescription(auction.getProductDescription())
                .receiveType(auction.getReceiveType())
                .contactPlace(auction.getContactPlace())
                .deliveryType(auction.getDeliveryType())
                .deliveryPrice(auction.getDeliveryPrice())
                .currentPrice(auction.getCurrentPrice())
                .startPrice(auction.getStartPrice())
                .instantPrice(auction.getInstantPrice())
                .memberCount(auction.getBidMemberCount())
                .endedAt(auction.getEndedAt())
                .seller(MemberGetDto.Response.from(auction.getSeller()))
                .parentCategory(CategoryGetDto.Response.from(auction.getParentCategory()))
                .childCategory(CategoryGetDto.Response.from(auction.getChildCategory()))
                .bidList(auction.getBidList() != null && !auction.getBidList().isEmpty()
                    ? auction.getBidList().stream().map(BidGetDto.Response::from).toList()
                    : new ArrayList<>())
                .askList(auction.getAskList() != null && !auction.getAskList().isEmpty()
                    ? auction.getAskList().stream().map(AskGetDto.Response::from).toList()
                    : new ArrayList<>())
                .imageList(auction.getImageList().stream().map(ImageGetDto.Response::from).toList())
                .createdAt(auction.getCreatedAt())
                .build();
        }
    }
}
