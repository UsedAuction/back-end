package com.ddang.usedauction.auction.domain;

import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.auction.listener.AuctionEntityListener;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.member.domain.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuctionEntityListener.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class Auction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 경매 제목

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuctionState auctionState; // 경매 상태

    @Column(nullable = false)
    private String productName; // 상품 이름

    @Column
    private String productColor; // 상품 색상

    @Column(nullable = false)
    private double productStatus; // 상품 상태

    @Column(length = 5000)
    private String productDescription; // 상품 설명

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReceiveType receiveType; // 거래 방식

    @Column
    private String contactPlace; // 대면 거래 장소

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType; // 택배비 타입

    @Column
    private String deliveryPrice; // 택배비

    @Column(nullable = false)
    private long currentPrice; // 현재 입찰가

    @Column(nullable = false)
    private long startPrice; // 입찰 시작가

    @Column(nullable = false)
    private long instantPrice; // 즉시 구매가

    @Column(nullable = false)
    private LocalDateTime endedAt; // 경매 마감일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller; // 판매자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id", nullable = false)
    private Category parentCategory; // 대분류 카테고리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_category_id", nullable = false)
    private Category childCategory; // 소분류 카테고리

    @OneToMany(mappedBy = "auction", fetch = FetchType.LAZY)
    private List<Bid> bidList; // 입찰 리스트

    @OneToMany(mappedBy = "auction", fetch = FetchType.LAZY)
    private List<Ask> askList; // 문의글 리스트

    @OneToMany(mappedBy = "auction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Image> imageList; // 이미지 리스트

    @Column
    private LocalDateTime deletedAt; // 삭제 날짜

    // 경매에 참여한 회원의 수
    public long getBidMemberCount() {

        if (bidList == null || bidList.isEmpty()) {
            return 0;
        }

        return bidList.stream()
            .map(b -> b.getMember().getMemberId())
            .collect(Collectors.toSet())
            .size();
    }

    // 연관관계인 이미지 엔티티 함께 저장하기 위한 메소드
    public void addImageList(Image image) {

        imageList = imageList == null ? new ArrayList<>() : imageList;
        imageList.add(image);
    }
}
