package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.CONTINUE;
import static com.ddang.usedauction.auction.domain.AuctionState.END;
import static com.ddang.usedauction.notification.domain.NotificationType.DONE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.exception.MemberPointOutOfBoundsException;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.auction.service.AuctionRedisService;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Disabled
class NotificationServiceInstantPurchaseTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuctionRedisService auctionRedisService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AuctionService auctionService;

    @Test
    @DisplayName("즉시구매시 경매종료 알림 전송 - 성공")
    void instantPurchase_notification_send_success() {
        //given
        Member seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .build();

        Member buyer = Member.builder()
            .id(2L)
            .memberId("buyer")
            .point(2000)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(CONTINUE)
            .instantPrice(2000)
            .seller(seller)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.of(buyer));

        //when
        auctionService.instantPurchaseAuction(auction.getId(), buyer.getMemberId());

        //then
        verify(notificationService).send(buyer.getId(), auction.getId(), "경매가 종료되었습니다.", DONE);
        verify(notificationService).send(seller.getId(), auction.getId(), "경매가 종료되었습니다.", DONE);
        verify(auctionRedisService).createAutoConfirm(auction.getId(), buyer.getMemberId(),
            auction.getInstantPrice(), seller.getId());
    }

    @Test
    @DisplayName("즉시구매시 경매종료 알림 전송 - 실패 (존재하지 않는 경매)")
    void instantPurchase_notification_send_fail_1() {
        // given
        Member seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .build();

        Member buyer = Member.builder()
            .id(2L)
            .memberId("buyer")
            .point(2000)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(CONTINUE)
            .instantPrice(2000)
            .seller(seller)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.empty());

        // when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.instantPurchaseAuction(auction.getId(), buyer.getMemberId()));

        // then
        verify(notificationService, times(0)).send(buyer.getId(), auction.getId(), "경매가 종료되었습니다.",
            DONE);
        verify(notificationService, times(0)).send(seller.getId(), auction.getId(), "경매가 종료되었습니다.",
            DONE);
    }

    @Test
    @DisplayName("즉시구매시 경매종료 알림 전송 - 실패 (존재하지 않는 회원)")
    void instantPurchase_notification_send_fail_2() {
        // given
        Member seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .build();

        Member buyer = Member.builder()
            .id(2L)
            .memberId("buyer")
            .point(2000)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(CONTINUE)
            .instantPrice(2000)
            .seller(seller)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.empty());

        // when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.instantPurchaseAuction(auction.getId(), buyer.getMemberId()));

        // then
        verify(notificationService, times(0)).send(buyer.getId(), auction.getId(), "경매가 종료되었습니다.",
            DONE);
        verify(notificationService, times(0)).send(seller.getId(), auction.getId(), "경매가 종료되었습니다.",
            DONE);
    }

    @Test
    @DisplayName("즉시구매시 경매종료 알림 전송 - 실패 (이미 종료된 경매)")
    void instantPurchase_notification_send_fail_3() {
        // given
        Member seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .build();

        Member buyer = Member.builder()
            .id(2L)
            .memberId("buyer")
            .point(2000)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(END)
            .instantPrice(2000)
            .seller(seller)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.of(buyer));

        // when
        assertThrows(IllegalStateException.class,
            () -> auctionService.instantPurchaseAuction(auction.getId(), buyer.getMemberId()));

        // then
        verify(notificationService, times(0)).send(buyer.getId(), auction.getId(), "경매가 종료되었습니다.",
            DONE);
        verify(notificationService, times(0)).send(seller.getId(), auction.getId(), "경매가 종료되었습니다.",
            DONE);
    }

    @Test
    @DisplayName("즉시구매시 경매종료 알림 전송 - 실패 (구매자의 포인트가 부족한 경우)")
    void instantPurchase_notification_send_fail_4() {
        // given
        Member seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .build();

        Member buyer = Member.builder()
            .id(2L)
            .memberId("buyer")
            .point(1000)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(CONTINUE)
            .instantPrice(2000)
            .seller(seller)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.of(buyer));

        // when
        assertThrows(MemberPointOutOfBoundsException.class,
            () -> auctionService.instantPurchaseAuction(auction.getId(), buyer.getMemberId()));

        // then
        verify(notificationService, times(0)).send(buyer.getId(), auction.getId(), "경매가 종료되었습니다.",
            DONE);
        verify(notificationService, times(0)).send(seller.getId(), auction.getId(), "경매가 종료되었습니다.",
            DONE);
    }
}