package com.ddang.usedauction.auction.listener;

import static com.ddang.usedauction.notification.domain.NotificationType.DONE;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto.Request;
import com.ddang.usedauction.auction.dto.AuctionEndDto;
import com.ddang.usedauction.auction.event.AuctionAutoConfirmEvent;
import com.ddang.usedauction.auction.event.AuctionEndEvent;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.auction.service.AuctionRedisService;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.chat.service.ChatMessageService;
import com.ddang.usedauction.chat.service.ChatRoomService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.service.NotificationService;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
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
    private final TransactionRepository transactionRepository;
    private final AuctionService auctionService;
    private final AuctionRedisService auctionRedisService;
    private final NotificationService notificationService;
    private final AuctionRepository auctionRepository;
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    // 경매 종료 이벤트 리스너
    @EventListener
    @Transactional
    public void handleAuctionEndEvent(AuctionEndEvent auctionEndEvent) {

        Long auctionId = auctionEndEvent.getAuctionId();

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        AuctionEndDto auctionEndDto = auctionService.endAuction(
            auctionId);// 경매 종료 처리 및 낙찰자

        Long sellerId = auctionEndDto.getSellerId(); // 판매자 PK
        Long buyerId = auctionEndDto.getBuyerId(); // 입찰자 PK, null인 경우 없음
        long price = auctionEndDto.getPrice(); // 판매한 가격

        if (buyerId != null) { // 낙찰자가 있는 경우
            Member buyer = memberRepository.findById(buyerId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

            // 일주일 후 자동 구매 확정 되도록 설정
            auctionRedisService.createAutoConfirm(auctionId, buyer.getMemberId(), price, sellerId);

            // 구매자에게 경매 종료 알림보내기
            sendNotificationForEnd(buyerId, auction);

            chatRoomService.createChatRoom(buyer.getId(), auction.getId());
        }

        // 판매자에게 경매 종료 알림보내기
        sendNotificationForEnd(sellerId, auction);
    }

    @EventListener
    public void handleAuctionAutoConfirmEvent(AuctionAutoConfirmEvent auctionAutoConfirmEvent) {

        Long auctionId = auctionAutoConfirmEvent.getAuctionId();
        String buyerId = auctionAutoConfirmEvent.getBuyerId();
        Request confirmDto = auctionAutoConfirmEvent.getConfirmDto();

        Transaction transaction = transactionRepository.findByBuyerIdAndAuctionId(buyerId,
                auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 거래 내역입니다."));

        // 구매확정으로 인해 이미 거래가 종료된 경우
        if (transaction.getTransType().equals(TransType.SUCCESS)) {
            return; // 종료
        }

        auctionService.confirmAuction(auctionId, buyerId, confirmDto);

        ChatRoom chatRoom = chatRoomService.deleteChatRoom(auctionId);
        chatMessageService.deleteMessagesByChatRoom(chatRoom.getId());
    }

    // 경매 종료 알림 전송
    private void sendNotificationForEnd(Long memberId, Auction auction) {

        notificationService.send(
            memberId,
            auction.getId(),
            auction.getTitle() + " 경매가 종료되었습니다.",
            DONE
        );
    }
}
