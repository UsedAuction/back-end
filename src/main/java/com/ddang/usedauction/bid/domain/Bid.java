package com.ddang.usedauction.bid.domain;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.bid.dto.BidServiceDto;
import com.ddang.usedauction.config.BaseTimeEntity;
import com.ddang.usedauction.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@SQLRestriction("deleted_at IS NULL")
public class Bid extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long bidPrice; // 입찰가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 입찰한 회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction; // 입찰한 경매

    @Column
    private LocalDateTime deletedAt; // 삭제 날짜

    // 입찰 엔티티를 서비스에서 사용할 dto로 변경
    public BidServiceDto toServiceDto() {

        return BidServiceDto.builder()
            .id(id)
            .bidPrice(bidPrice)
            .auction(auction.toServiceDto())
            .member(member.toServiceDto())
            .createdAt(getCreatedAt())
            .build();
    }
}
