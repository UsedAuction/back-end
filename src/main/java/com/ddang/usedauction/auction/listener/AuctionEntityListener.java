package com.ddang.usedauction.auction.listener;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.service.AuctionRedisService;
import com.ddang.usedauction.util.BeanUtils;
import jakarta.persistence.PostPersist;
import java.time.Duration;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuctionEntityListener { // 경매 엔티티 리스너

    // 경매 생성 시
    @PostPersist
    public void postPersist(Auction auction) {

        AuctionRedisService auctionRedisService = BeanUtils.getBean(AuctionRedisService.class);
        auctionRedisService.createWithExpire(auction, getExpireSecond(auction));
    }

    // 경매의 종료 기간 가져오기
    private long getExpireSecond(Auction auction) {

        return Duration.between(
            auction.getCreatedAt(),
            auction.getEndedAt()
        ).getSeconds();
    }
}
