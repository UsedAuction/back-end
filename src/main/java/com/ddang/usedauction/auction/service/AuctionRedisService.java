package com.ddang.usedauction.auction.service;

import com.ddang.usedauction.auction.domain.Auction;
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
}
