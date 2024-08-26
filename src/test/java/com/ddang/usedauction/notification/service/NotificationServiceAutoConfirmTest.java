package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.END;
import static com.ddang.usedauction.notification.domain.NotificationType.CONFIRM;
import static com.ddang.usedauction.point.domain.PointType.GET;
import static com.ddang.usedauction.point.domain.PointType.USE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
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
class NotificationServiceAutoConfirmTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AuctionService auctionService;

    private Member seller;
    private Member buyer;
    private Auction auction;

    private AuctionConfirmDto.Request confirmDto;
    private Transaction buyerTransaction;
    private PointHistory pointHistory_buyer;
    private PointHistory pointHistory_seller;

    @BeforeEach
    void before() {

        seller = Member.builder()
            .id(1L)
            .email("seller@example.com")
            .memberId("seller")
            .build();

        buyer = Member.builder()
            .id(2L)
            .email("buyer@example.com")
            .memberId("buyer")
            .build();

        auction = Auction.builder()
            .id(1L)
            .auctionState(END)
            .seller(seller)
            .build();

        confirmDto = AuctionConfirmDto.Request.builder()
            .sellerId(seller.getId())
            .build();

        pointHistory_buyer = PointHistory.builder()
            .id(1L)
            .pointType(USE)
            .member(buyer)
            .build();

        pointHistory_seller = PointHistory.builder()
            .id(2L)
            .pointType(GET)
            .member(seller)
            .build();

        buyerTransaction = Transaction.builder()
            .id(1L)
            .transType(TransType.CONTINUE)
            .build();
    }

    @Test
    @DisplayName("구매확정시 판매자, 구매자에게 알림 전송 - 성공")
    void auto_confirm_success() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(transactionRepository.findByBuyerEmailAndAuctionId(buyer.getEmail(), auction.getId()))
            .willReturn(Optional.of(buyerTransaction));
        given(memberRepository.findByEmail(buyer.getEmail())).willReturn(Optional.of(buyer));
        given(memberRepository.findById(seller.getId())).willReturn(Optional.of(seller));

        //when
        auctionService.confirmAuction(auction.getId(), buyer.getEmail(), confirmDto);

        //then
        verify(notificationService, times(1))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );

        verify(notificationService, times(1))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자, 구매자에게 알림 전송 - 실패 (존재하지 않는 경매)")
    void auto_confirm_fail_1() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getEmail(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );

        verify(notificationService, times(0))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자, 구매자에게 알림 전송 - 실패 (진행중인 경매)")
    void auto_confirm_fail_2() {

        //given
        Auction auction_continue = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.CONTINUE)
            .instantPrice(2000)
            .seller(seller)
//            .bidList(bidList)
            .build();

        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction_continue));

        //when
        assertThrows(IllegalStateException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getMemberId(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );

        verify(notificationService, times(0))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자, 구매자에게 알림 전송 - 실패 (존재하지 않는 구매자)")
    void auto_confirm_fail_3() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(transactionRepository.findByBuyerEmailAndAuctionId(buyer.getEmail(), auction.getId()))
            .willReturn(Optional.of(buyerTransaction));
        given(memberRepository.findByEmail(buyer.getEmail())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getEmail(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );

        verify(notificationService, times(0))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자, 구매자에게 알림 전송 - 실패 (존재하지 않는 판매자)")
    void auto_confirm_fail_4() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(transactionRepository.findByBuyerEmailAndAuctionId(buyer.getEmail(), auction.getId()))
            .willReturn(Optional.of(buyerTransaction));
        given(memberRepository.findByEmail(buyer.getEmail())).willReturn(Optional.of(buyer));
        given(memberRepository.findById(confirmDto.getSellerId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getEmail(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );

        verify(notificationService, times(0))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자, 구매자에게 알림 전송 - 실패 (구매자의 거래내역이 존재하지 않음)")
    void auto_confirm_fail_5() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(transactionRepository.findByBuyerEmailAndAuctionId(buyer.getEmail(), auction.getId()))
            .willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getEmail(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );

        verify(notificationService, times(0))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }
}