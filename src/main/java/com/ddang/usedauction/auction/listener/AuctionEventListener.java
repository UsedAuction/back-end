package com.ddang.usedauction.auction.listener;

import static com.ddang.usedauction.notification.domain.NotificationType.*;
import static com.ddang.usedauction.notification.domain.NotificationType.DONE;

import com.ddang.usedauction.auction.dto.AuctionConfirmDto.Request;
import com.ddang.usedauction.auction.event.AuctionAutoConfirmEvent;
import com.ddang.usedauction.auction.event.AuctionEndEvent;
import com.ddang.usedauction.auction.service.AuctionRedisService;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.service.NotificationService;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventListener { // 경매 이벤트 리스너

    private final MemberRepository memberRepository;
    private final AuctionService auctionService;
    private final AuctionRedisService auctionRedisService;
    private final NotificationService notificationService;

    // 경매 종료 이벤트 리스너
    @EventListener
    @Transactional
    public void handleAuctionEndEvent(AuctionEndEvent auctionEndEvent) {

        Long auctionId = auctionEndEvent.getAuctionId();

        Map<String, Long> auctionAndMemberMap = auctionService.endAuction(
            auctionId);// 경매 종료 처리 및 낙찰자

        Long sellerId = auctionAndMemberMap.get("seller"); // 판매자 PK
        Long buyerId = auctionAndMemberMap.get("buyer"); // 입찰자 PK, null인 경우 없음
        Long price = auctionAndMemberMap.get("price");

        if (buyerId != null) { // 낙찰자가 있는 경우
            Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

            // 일주일 후 자동 구매 확정 되도록 설정
            auctionRedisService.createAutoConfirm(auctionId, buyer.getMemberId(), price, sellerId);

            // 구매자에게 경매 종료 알림보내기
            notificationService.send(buyerId, "경매가 종료되었습니다.", DONE);

            // todo: 판매자 및 낙찰자 채팅방 생성
        }

        // 판매자에게 경매 종료 알림보내기
        notificationService.send(sellerId, "경매가 종료되었습니다.", DONE);
    }

    @EventListener
    public void handleAuctionAutoConfirmEvent(AuctionAutoConfirmEvent auctionAutoConfirmEvent) {

        Long auctionId = auctionAutoConfirmEvent.getAuctionId();
        String buyerId = auctionAutoConfirmEvent.getBuyerId();
        Request confirmDto = auctionAutoConfirmEvent.getConfirmDto();

        auctionService.confirmAuction(auctionId, buyerId, confirmDto);

        // 판매자에게 구매 확정 알림보내기
        notificationService.send(confirmDto.getSellerId(), "구매가 확정되었습니다.", CONFIRM);
    }
}
