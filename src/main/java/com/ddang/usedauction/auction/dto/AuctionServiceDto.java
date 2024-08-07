package com.ddang.usedauction.auction.dto;

import com.ddang.usedauction.ask.dto.AskServiceDto;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.TransactionType;
import com.ddang.usedauction.bid.dto.BidServiceDto;
import com.ddang.usedauction.category.dto.CategoryServiceDto;
import com.ddang.usedauction.image.dto.ImageServiceDto;
import com.ddang.usedauction.member.dto.MemberServiceDto;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 서비스에서 사용할 dto
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
    private TransactionType transactionType; // 거래 방식
    private String contactPlace; // 대면 거래 장소
    private DeliveryType deliveryType; // 택배비 타입
    private String deliveryPrice; // 택배비
    private long currentPrice; // 현재 입찰가
    private long startPrice; // 입찰 시작가
    private long instantPrice; // 즉시 구매가
    private List<BidServiceDto> bidList; // 입찰 리스트
    private MemberServiceDto seller; // 판매자
    private CategoryServiceDto parentCategory; // 대분류 카테고리
    private CategoryServiceDto childCategory; // 소분류 카테고리
    private List<AskServiceDto> ask; // 문의글
    private List<ImageServiceDto> imageList; // 이미지 리스트

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime endedAt; // 경매 마감일

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDateTime createdAt; // 생성 날짜

    // 경매 생성 완료 시 response로 변경하는 메소드
    public AuctionCreateDto.Response toCreateResponse() {

        return AuctionCreateDto.Response.builder()
            .id(id)
            .title(title)
            .build();
    }
}
