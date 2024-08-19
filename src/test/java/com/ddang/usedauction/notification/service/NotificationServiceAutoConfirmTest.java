package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.CONTINUE;
import static com.ddang.usedauction.auction.domain.AuctionState.END;
import static com.ddang.usedauction.notification.domain.NotificationType.CONFIRM;
import static com.ddang.usedauction.transaction.domain.TransType.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.event.AuctionAutoConfirmEvent;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import com.ddang.usedauction.point.type.PointType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private List<Bid> bidList;
    private Auction auction;
    private AuctionConfirmDto.Request auctionConfirmRequest;
    private AuctionAutoConfirmEvent auctionAutoConfirmEvent;

    private AuctionConfirmDto.Request confirmDto;
    private PointHistory pointHistory_buyer;
    private PointHistory pointHistory_seller;
    private Transaction buyerTransaction;

    @BeforeEach
    void before() {

        seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .point(0)
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
            .auctionState(END)
            .instantPrice(2000)
            .seller(seller)
            .bidList(bidList)
            .build();

        confirmDto = AuctionConfirmDto.Request.builder()
            .price(2000)
            .sellerId(seller.getId())
            .build();

        pointHistory_buyer = PointHistory.builder()
            .id(1L)
            .pointType(PointType.USE)
            .pointAmount(2000)
            .curPointAmount(10000)
            .member(buyer)
            .build();

        pointHistory_seller = PointHistory.builder()
            .id(2L)
            .pointType(PointType.GET)
            .pointAmount(2000)
            .curPointAmount(2000)
            .member(seller)
            .build();

        buyerTransaction = Transaction.builder()
            .id(1L)
            .price(2000)
            .transType(SUCCESS)
//            .buyType()
            .buyer(buyer)
            .auction(auction)
            .build();
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 성공")
    void auto_confirm_success() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.of(buyer));
        given(memberRepository.findById(seller.getId())).willReturn(Optional.of(seller));
        given(transactionRepository.findByBuyerId(buyer.getId(), auction.getId()))
            .willReturn(Optional.of(Transaction.builder().build()));

        ArgumentCaptor<Member> sellerCaptor = ArgumentCaptor.forClass(Member.class);
        ArgumentCaptor<PointHistory> pointHistoryCaptor = ArgumentCaptor.forClass(PointHistory.class);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);

        //when
        auctionService.confirmAuction(auction.getId(), buyer.getMemberId(), confirmDto);

        //then
        // memberRepository
        verify(memberRepository, times(1)).save(sellerCaptor.capture());
        assertEquals(2000, sellerCaptor.getValue().getPoint());

        // pointRepository
        verify(pointRepository, times(2)).save(pointHistoryCaptor.capture());

        List<PointHistory> captorPointHistories = pointHistoryCaptor.getAllValues();
        PointHistory buyerPointHistory = captorPointHistories.stream()
            .filter(cap -> cap.getPointType() == PointType.USE)
            .findFirst()
            .orElseThrow(AssertionError::new);
        assertEquals(buyer.getId(), buyerPointHistory.getMember().getId());
        assertEquals(2000, buyerPointHistory.getPointAmount());

        PointHistory sellerPointHistory = captorPointHistories.stream()
            .filter(cap -> cap.getPointType() == PointType.GET)
            .findFirst()
            .orElseThrow(AssertionError::new);
        assertEquals(seller.getId(), sellerPointHistory.getMember().getId());
        assertEquals(2000, sellerPointHistory.getPointAmount());

        // transactionRepository
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        assertEquals(SUCCESS, transactionCaptor.getValue().getTransType());

        // notificationService
        verify(notificationService, times(1))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 실패 (존재하지 않는 경매)")
    void auto_confirm_fail_1() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getMemberId(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 실패 (진행중인 경매)")
    void auto_confirm_fail_2() {

        //given
        Auction auction_continue = Auction.builder()
            .id(1L)
            .auctionState(CONTINUE)
            .instantPrice(2000)
            .seller(seller)
            .bidList(bidList)
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
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 실패 (존재하지 않는 구매자)")
    void auto_confirm_fail_3() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getMemberId(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 실패 (존재하지 않는 판매자)")
    void auto_confirm_fail_4() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.of(buyer));
        given(memberRepository.findById(confirmDto.getSellerId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getMemberId(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }

    @Test
    @DisplayName("구매확정시 판매자에게 알림 전송 - 실패 (구매자의 거래내역이 존재하지 않음)")
    void auto_confirm_fail_5() {

        //given
        given(auctionRepository.findById(auction.getId())).willReturn(Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.of(buyer));
        given(memberRepository.findById(confirmDto.getSellerId())).willReturn(Optional.of(seller));
        given(transactionRepository.findByBuyerId(buyer.getId(), auction.getId()))
            .willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(auction.getId(), buyer.getMemberId(), confirmDto));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
                CONFIRM
            );
    }
}