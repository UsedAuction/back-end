package com.ddang.usedauction.auction.service;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.repository.AuctionRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionRedisService {

    private final AuctionRedisRepository auctionRedisRepository;

    // 경매가 종료되는 날짜에 맞춰 expire 설정하여 redis에 저장
    public void createWithExpire(Auction auction, long expireSecond) {

        auctionRedisRepository.saveAuctionWithExpire(auction.getId(), expireSecond);
    }

    // 자동 구매 확정을 위한 bucket 생성 서비스
    public void createAutoConfirm(Long auctionId, String buyerId, long price, Long sellerId) {

        auctionRedisRepository.saveAuctionAutoConfirm(auctionId, buyerId,
            AuctionConfirmDto.Request.from(price, sellerId));
    }
}
