package com.ddang.usedauction.auction.listener;

import com.ddang.usedauction.auction.event.AuctionEndEvent;
import com.ddang.usedauction.auction.service.AuctionService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventListener { // 경매 이벤트 리스너

    private final AuctionService auctionService;

    // 경매 종료 이벤트 리스너
    @EventListener
    public void handleAuctionEndEvent(AuctionEndEvent auctionEndEvent) {

        Long auctionId = auctionEndEvent.getAuctionId();

        Map<String, Long> auctionAndMemberMap = auctionService.endAuction(
            auctionId);// 경매 종료 처리 및 낙찰자

        Long sellerId = auctionAndMemberMap.get("seller"); // 판매자 PK
        Long buyerId = auctionAndMemberMap.get("buyer"); // 입찰자 PK, null인 경우 없음

        // todo: 경매 종료 알림(판매자 및 낙찰자) 및 채팅방 생성
    }
}
