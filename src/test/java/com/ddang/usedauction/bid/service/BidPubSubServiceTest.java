package com.ddang.usedauction.bid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.bid.dto.BidErrorMessageDto;
import com.ddang.usedauction.bid.dto.BidFailByPointMessageDto.Response;
import com.ddang.usedauction.bid.dto.BidFailByPreviousBidMessageDto;
import com.ddang.usedauction.bid.dto.BidMessageDto;
import com.ddang.usedauction.bid.dto.BidStatus;
import com.ddang.usedauction.bid.repository.BidRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
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
import org.springframework.messaging.simp.SimpMessageSendingOperations;

@ExtendWith(MockitoExtension.class)
class BidPubSubServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SimpMessageSendingOperations simpMessageSendingOperations;

    @InjectMocks
    private BidPubSubService bidPubSubService;

    BidMessageDto.Request message;
    Auction auction;
    Member member;

    @BeforeEach
    void setup() {

        message = BidMessageDto.Request.builder()
            .bidAmount(3000)
            .auctionId(1L)
            .memberId("test")
            .build();

        Member seller = Member.builder()
            .id(2L)
            .build();

        auction = Auction.builder()
            .id(1L)
            .currentPrice(2000)
            .instantPrice(4000)
            .startPrice(2000)
            .auctionState(AuctionState.CONTINUE)
            .seller(seller)
            .build();

        member = Member.builder()
            .id(1L)
            .memberId("test")
            .email("test@naver.com")
            .point(10000)
            .build();
    }

    @Test
    @DisplayName("입찰 pub/sub")
    void createBid() {

        Bid bid = Bid.builder()
            .id(1L)
            .bidPrice(1000)
            .member(member)
            .build();
        List<Bid> bidList = List.of(bid);

        Auction anotherAuction = Auction.builder()
            .id(2L)
            .bidList(bidList)
            .build();
        List<Auction> auctionList = List.of(anotherAuction);

        Auction savedAuction = auction.toBuilder()
            .currentPrice(message.getBidAmount())
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.of(member));
        when(auctionRepository.findAllByMemberIdAndAuctionState("test",
            AuctionState.CONTINUE)).thenReturn(auctionList);
        when(auctionRepository.save(argThat(arg -> arg.getId().equals(1L)))).thenReturn(
            savedAuction);

        bidPubSubService.createBid(message);

        assertEquals(3000, savedAuction.getCurrentPrice());
        verify(simpMessageSendingOperations, times(1)).convertAndSend(
            argThat(arg -> arg.equals("/sub/auction/1")), (Object) argThat(arg -> {
                if (!(arg instanceof BidMessageDto.Response)) {
                    return false;
                }

                BidMessageDto.Response response = BidMessageDto.Response.from(message, "test");

                return response.getBidAmount() == 3000;
            }));
        verify(simpMessageSendingOperations, times(1)).convertAndSend(
            argThat(arg -> arg.equals("/sub/auction-all")), (Object) argThat(arg -> {
                if (!(arg instanceof BidMessageDto.Response)) {
                    return false;
                }

                BidMessageDto.Response response = BidMessageDto.Response.from(message, "test");

                return response.getBidAmount() == 3000;
            }));
    }

    @Test
    @DisplayName("입찰 pub/sub 실패 - 존재하지 않는 경매")
    void createBidFail1() {

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> bidPubSubService.createBid(message));
    }

    @Test
    @DisplayName("입찰 pub/sub 실패 - 존재하지 않는 회원")
    void createBidFail6() {

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> bidPubSubService.createBid(message));
    }

    @Test
    @DisplayName("입찰 pub/sub 실패 - 이미 종료된 경매")
    void createBidFail2() {

        auction = auction.toBuilder()
            .auctionState(AuctionState.END)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.of(member));

        assertThrows(IllegalStateException.class,
            () -> bidPubSubService.createBid(message));

        verify(simpMessageSendingOperations, times(1)).convertAndSend(
            argThat(arg -> arg.equals("/sub/errors/test")),
            (Object) (argThat(arg -> {
                if (!(arg instanceof BidErrorMessageDto)) {
                    return false;
                }

                BidErrorMessageDto bidErrorMessageDto = BidErrorMessageDto.from("이미 종료된 경매입니다.");
                return bidErrorMessageDto.getMessage().equals("이미 종료된 경매입니다.");
            })));
    }

    @Test
    @DisplayName("입찰 pub/sub 실패 - 즉시 구매가 이상으로 입찰 시도")
    void createBidFail3() {

        message = message.toBuilder()
            .bidAmount(auction.getInstantPrice())
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.of(member));

        assertThrows(IllegalArgumentException.class,
            () -> bidPubSubService.createBid(message));

        verify(simpMessageSendingOperations, times(1)).convertAndSend(
            argThat(arg -> arg.equals("/sub/errors/test")),
            (Object) argThat(arg -> {
                if (!(arg instanceof BidErrorMessageDto)) {
                    return false;
                }

                BidErrorMessageDto bidErrorMessageDto = BidErrorMessageDto.from(
                    "즉시구매가 이상으로 입찰할 수 없습니다.");
                return bidErrorMessageDto.getMessage().equals("즉시구매가 이상으로 입찰할 수 없습니다.");
            }));
    }

    @Test
    @DisplayName("입찰 pub/sub 실패 - 현재 입찰 시도한 금액 이상으로 먼저 입찰한 회원이 존재하는 경우")
    void createBidFail4() {

        auction = auction.toBuilder()
            .currentPrice(auction.getStartPrice() + 1000)
            .build();

        message = message.toBuilder()
            .bidAmount(auction.getCurrentPrice())
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.of(member));

        assertThrows(IllegalArgumentException.class,
            () -> bidPubSubService.createBid(message));

        verify(simpMessageSendingOperations, times(1)).convertAndSend(
            argThat(arg -> arg.equals("/sub/errors/test")),
            (Object) argThat(arg -> {
                if (!(arg instanceof BidFailByPreviousBidMessageDto.Response)) {
                    return false;
                }

                BidFailByPreviousBidMessageDto.Response response = BidFailByPreviousBidMessageDto.Response.from(
                    auction.getCurrentPrice(), message.getBidAmount());
                return response.getBidStatus().equals(BidStatus.BID_FAIL);
            }));
    }

    @Test
    @DisplayName("입찰 pub/sub 실패 - 입찰 시도한 금액이 현재 입찰 시작가의 +1000을 한 금액보다 작은 경우")
    void createBidFail5() {

        message = message.toBuilder()
            .bidAmount(auction.getStartPrice())
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.of(member));

        assertThrows(IllegalArgumentException.class,
            () -> bidPubSubService.createBid(message));

        verify(simpMessageSendingOperations, times(1)).convertAndSend(
            argThat(arg -> arg.equals("/sub/errors/test")),
            (Object) argThat(arg -> {
                if (!(arg instanceof BidErrorMessageDto)) {
                    return false;
                }

                BidErrorMessageDto bidErrorMessageDto = BidErrorMessageDto.from(
                    "입찰 금액은 입찰 시작가보다 1000원 높은 금액부터 입찰할 수 있습니다.");
                return bidErrorMessageDto.getMessage()
                    .equals("입찰 금액은 입찰 시작가보다 1000원 높은 금액부터 입찰할 수 있습니다.");
            }));
    }

    @Test
    @DisplayName("입찰 pub/sub 실패 - 현재 보유 포인트에서 다른 경매에서 현재 회원이 최고입찰자인 경우 해당 포인트만큼 감소시킨 금액이 현재 입찰할 금액보다 적은 경우")
    void createBidFail7() {

        Bid bid = Bid.builder()
            .id(1L)
            .bidPrice(member.getPoint())
            .member(member)
            .build();
        List<Bid> bidList = List.of(bid);

        Auction anotherAuction = Auction.builder()
            .id(2L)
            .bidList(bidList)
            .build();
        List<Auction> auctionList = List.of(anotherAuction);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.of(member));
        when(auctionRepository.findAllByMemberIdAndAuctionState("test",
            AuctionState.CONTINUE)).thenReturn(auctionList);

        assertThrows(IllegalArgumentException.class,
            () -> bidPubSubService.createBid(message));

        verify(simpMessageSendingOperations, times(1)).convertAndSend(
            argThat(arg -> arg.equals("/sub/errors/test")),
            (Object) argThat(arg -> {
                if (!(arg instanceof Response)) {
                    return false;
                }

                Response response = Response.from(member.getPoint(), member.getPoint(),
                    message.getBidAmount());
                return response.getBidStatus().equals(BidStatus.POINT_FAIL);
            }));
    }
}