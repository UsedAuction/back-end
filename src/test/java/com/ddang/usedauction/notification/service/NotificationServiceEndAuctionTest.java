package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.CONTINUE;
import static com.ddang.usedauction.notification.domain.NotificationType.DONE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.dto.AuctionEndDto;
import com.ddang.usedauction.auction.event.AuctionEndEvent;
import com.ddang.usedauction.auction.listener.AuctionEventListener;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.auction.service.AuctionRedisService;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceEndAuctionTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuctionService auctionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuctionRedisService auctionRedisService;

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private AuctionEventListener auctionEventListener;

    private AuctionEndEvent auctionEndEvent;
    private Member seller;
    private Member buyer;
    private List<Bid> bidList;
    private Auction auction;

    @BeforeEach
    void before() {

        auctionEndEvent = AuctionEndEvent.builder()
            .auctionId(1L)
            .build();

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
    }

    @Test
    @DisplayName("낙찰자가 있는 경매종료 알림 전송 - 성공")
    void exist_buyer_endAuction_send_success() {

        //given
        AuctionEndDto auctionEndDto = AuctionEndDto.builder()
            .sellerId(seller.getId())
            .buyerId(buyer.getId())
            .price(2000)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.ofNullable(auction));
        given(auctionService.endAuction(auction.getId())).willReturn(auctionEndDto);
        given(memberRepository.findById(buyer.getId())).willReturn(Optional.of(buyer));

        //when
        auctionEventListener.handleAuctionEndEvent(auctionEndEvent);

        //then
        verify(notificationService, times(1))
            .send(buyer.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
        verify(notificationService, times(1))
            .send(seller.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
    }

    @Test
    @DisplayName("낙찰자가 있는 경매종료 알림 전송 - 실패 (존재하지 않는 경매)")
    void exist_buyer_endAuction_send_fail_1() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionEventListener.handleAuctionEndEvent(auctionEndEvent));

        //then
        verify(notificationService, times(0))
            .send(buyer.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
        verify(notificationService, times(0))
            .send(seller.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
    }

    @Test
    @DisplayName("낙찰자가 있는 경매종료 알림 전송 - 실패 (이미 종료된 경매)")
    void exist_buyer_endAuction_send_fail_2() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.ofNullable(auction));
        given(auctionService.endAuction(auction.getId())).willThrow(new IllegalStateException("현재 경매가 이미 종료되었습니다."));

        //when
        assertThrows(IllegalStateException.class,
            () -> auctionEventListener.handleAuctionEndEvent(auctionEndEvent));

        //then
        verify(notificationService, times(0))
            .send(buyer.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
        verify(notificationService, times(0))
            .send(seller.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
    }

    @Test
    @DisplayName("낙찰자가 있는 경매종료 알림 전송 - 실패 (존재하지 않는 낙찰자)")
    void exist_buyer_endAuction_send_fail_3() {

        //given
        AuctionEndDto auctionEndDto = AuctionEndDto.builder()
            .sellerId(seller.getId())
            .buyerId(buyer.getId())
            .price(2000)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.ofNullable(auction));
        given(memberRepository.findById(buyer.getId())).willThrow(new NoSuchElementException("존재하지 않는 회원입니다."));

        //when
        given(auctionService.endAuction(auction.getId())).willReturn(auctionEndDto);
        assertThrows(NoSuchElementException.class,
            () -> auctionEventListener.handleAuctionEndEvent(auctionEndEvent));

        //then
        verify(notificationService, times(0))
            .send(buyer.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
        verify(notificationService, times(0))
            .send(seller.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
    }

    @Test
    @DisplayName("낙찰자가 없는 경매종료 알림 전송 - 성공")
    void not_exist_buyer_endAuction_send_success() {

        //given
        AuctionEndDto auctionEndDto = AuctionEndDto.builder()
            .sellerId(seller.getId())
            .buyerId(null)
            .price(2000)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.ofNullable(auction));
        given(auctionService.endAuction(auction.getId())).willReturn(auctionEndDto);


        //when
        auctionEventListener.handleAuctionEndEvent(auctionEndEvent);

        //then
        verify(notificationService, times(0))
            .send(buyer.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
        verify(notificationService, times(1))
            .send(seller.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
    }

    @Test
    @DisplayName("낙찰자가 없는 경매종료 알림 전송 - 실패 (존재하지 않는 경매)")
    void not_exist_buyer_endAuction_send_fail_1() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.ofNullable(auction));
        given(auctionService.endAuction(auction.getId())).willThrow(new NoSuchElementException("존재하지 않는 경매입니다."));

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionEventListener.handleAuctionEndEvent(auctionEndEvent));

        //then
        verify(notificationService, times(0))
            .send(buyer.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
        verify(notificationService, times(0))
            .send(seller.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
    }

    @Test
    @DisplayName("낙찰자가 없는 경매종료 알림 전송 - 실패 (이미 종료된 경매)")
    void not_exist_buyer_endAuction_send_fail_2() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.ofNullable(auction));
        given(auctionService.endAuction(auction.getId())).willThrow(new IllegalStateException("현재 경매가 이미 종료되었습니다."));
      
        //when
        assertThrows(IllegalStateException.class,
            () -> auctionEventListener.handleAuctionEndEvent(auctionEndEvent));

        //then
        verify(notificationService, times(0))
            .send(buyer.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
        verify(notificationService, times(0))
            .send(seller.getId(), auction.getId(), auction.getTitle() + " 경매가 종료되었습니다.", DONE);
    }
}