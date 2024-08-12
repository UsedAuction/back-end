package com.ddang.usedauction.auction.listener;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.service.AuctionRedisService;
import jakarta.persistence.PostPersist;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionEntityListener { // 경매 엔티티 리스너

    private final AuctionRedisService auctionRedisService;

    // 경매 생성 후 실행될 메서드
    @PostPersist
    public void postPersist(Auction auction) {

        // auction 객체와 만료 시간(expire)을 사용하여 Redis에 경매 데이터를 저장합니다.
        auctionRedisService.createWithExpire(auction, getExpireSecond(auction));
    }

    // 경매의 종료 기간(만료 시간)을 초 단위로 계산하여 반환하는 메서드
    // auction의 생성 시간(createdAt)과 종료 시간(endedAt) 간의 차이를 구함
    private long getExpireSecond(Auction auction) {

        return Duration.between(
            auction.getCreatedAt(),
            auction.getEndedAt()
        ).getSeconds();
    }
}
