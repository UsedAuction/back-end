package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.CONTINUE;
import static com.ddang.usedauction.notification.domain.NotificationType.CONFIRM;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.event.AuctionAutoConfirmEvent;
import com.ddang.usedauction.auction.listener.AuctionEventListener;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.member.domain.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceAutoConfirmTest {

    @Mock
    private AuctionService auctionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuctionEventListener auctionEventListener;

    private Member seller;
    private Member buyer;
    private List<Bid> bidList;
    private Auction auction;
    private AuctionConfirmDto.Request auctionConfirmRequest;
    private AuctionAutoConfirmEvent auctionAutoConfirmEvent;

    @BeforeEach
    void before() {

        seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .build();

        buyer = Member.builder()
            .id(2L)
            .memberId("buyer")
            .build();

        Bid bid = Bid.builder()
            .bidPrice(1000)
            .build();

        bidList = new ArrayList<>();
        bidList.add(bid);

        auction = Auction.builder()
            .id(1L)
            .auctionState(CONTINUE)
            .instantPrice(2000)
            .seller(seller)
            .bidList(bidList)
            .build();

        auctionConfirmRequest = AuctionConfirmDto.Request.builder()
            .price(2000L)
            .sellerId(seller.getId())
            .build();

        auctionAutoConfirmEvent = AuctionAutoConfirmEvent.builder()
            .auctionId(auction.getId())
            .buyerId(buyer.getMemberId())
            .confirmDto(auctionConfirmRequest)
            .build();
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 성공")
    void auto_confirm_success() {

        //given
        doNothing().when(auctionService).confirmAuction(auction.getId(), buyer.getMemberId(), auctionConfirmRequest);

        //when
        auctionEventListener.handleAuctionAutoConfirmEvent(auctionAutoConfirmEvent);

        //then
        verify(notificationService, times(1)).send(seller.getId(), "구매가 확정되었습니다.", CONFIRM);
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 실패 (존재하지 않는 경매)")
    void auto_confirm_fail_1() {

        //given
        doThrow(new NoSuchElementException("존재하지 않는 경매입니다."))
            .when(auctionService).confirmAuction(auction.getId(), buyer.getMemberId(), auctionConfirmRequest);

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionEventListener.handleAuctionAutoConfirmEvent(auctionAutoConfirmEvent));

        //then
        verify(notificationService, times(0)).send(seller.getId(), "구매가 확정되었습니다.", CONFIRM);
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 실패 (진행중인 경매)")
    void auto_confirm_fail_2() {

        //given
        doThrow(new IllegalStateException("진행 중인 경매에는 구매 확정을 할 수 없습니다."))
            .when(auctionService).confirmAuction(auction.getId(), buyer.getMemberId(), auctionConfirmRequest);

        //when
        assertThrows(IllegalStateException.class,
            () -> auctionEventListener.handleAuctionAutoConfirmEvent(auctionAutoConfirmEvent));

        //then
        verify(notificationService, times(0)).send(seller.getId(), "구매가 확정되었습니다.", CONFIRM);
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 실패 (존재하지 않는 회원)")
    void auto_confirm_fail_3() {

        //given
        doThrow(new NoSuchElementException("존재하지 않는 회원입니다."))
            .when(auctionService).confirmAuction(auction.getId(), buyer.getMemberId(), auctionConfirmRequest);

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionEventListener.handleAuctionAutoConfirmEvent(auctionAutoConfirmEvent));

        //then
        verify(notificationService, times(0)).send(seller.getId(), "구매가 확정되었습니다.", CONFIRM);
    }
}