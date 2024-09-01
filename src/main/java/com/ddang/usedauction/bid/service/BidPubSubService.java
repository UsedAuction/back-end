package com.ddang.usedauction.bid.service;

import com.ddang.usedauction.aop.RedissonLock;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.bid.dto.BidErrorMessageDto;
import com.ddang.usedauction.bid.dto.BidFailByPointMessageDto;
import com.ddang.usedauction.bid.dto.BidFailByPreviousBidMessageDto;
import com.ddang.usedauction.bid.dto.BidMessageDto;
import com.ddang.usedauction.bid.dto.BidMessageDto.Request;
import com.ddang.usedauction.bid.repository.BidRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidPubSubService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    /**
     * 입찰하기
     *
     * @param message 입찰 정보
     */
    @RedissonLock("#message.auctionId")
    public void createBid(BidMessageDto.Request message) {

        log.info("auctionId = {}", message.getAuctionId());
        log.info("memberId = {}", message.getMemberId());
        log.info("bidAmount = {}", message.getBidAmount());

        Auction auction = auctionRepository.findById(message.getAuctionId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        log.info("startPrice = {}", auction.getStartPrice());
        log.info("currentPrice = {}", auction.getCurrentPrice());
        log.info("instantPrice = {}", auction.getInstantPrice());

        Member member = memberRepository.findByMemberId(message.getMemberId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        // 검증 및 에러메시지 발송
        validationAndSendErrorMessage(message, auction, member);

        auction = auction.toBuilder()
            .currentPrice(message.getBidAmount())
            .build();
        Auction savedAuction = auctionRepository.save(auction);

        Bid bid = Bid.builder()
            .auction(savedAuction)
            .bidPrice(message.getBidAmount())
            .member(member)
            .build();
        bidRepository.save(bid);

        // 해당 경매 채널에 성공 메시지 발송
        simpMessageSendingOperations.convertAndSend(
            "/sub/auction/" + message.getAuctionId() + "/" + message.getMemberId(),
            BidMessageDto.Response.from(message, message.getMemberId()));

        // 경매 리스트 채널에 성공 메시지 발송
        simpMessageSendingOperations.convertAndSend("/sub/auction-all",
            BidMessageDto.Response.from(message, message.getMemberId()));
    }

    // 검증 및 에러 상황에 맞는 메시지를 해당 유저에게 발송
    private void validationAndSendErrorMessage(Request message, Auction auction, Member member) {

        // 판매자가 입찰을 진행하려고 하는 경우
        if (auction.getSeller().getId().equals(member.getId())) {
            throw new IllegalStateException("판매자가 입찰을 진행할 수 없습니다.");
        }

        // 종료된 경매인 경우
        if (auction.getAuctionState().equals(AuctionState.END)) {
            // 해당 회원에게 에러 메시지 발송
            simpMessageSendingOperations.convertAndSend("/sub/errors/" + message.getMemberId(),
                BidErrorMessageDto.from("이미 종료된 경매입니다."));

            throw new IllegalStateException("이미 종료된 경매입니다.");
        }

        // 즉시구매가 이상으로 입찰 시도하는 경우
        if (message.getBidAmount() >= auction.getInstantPrice()) {
            // 해당 회원에게 에러 메시지 발송
            simpMessageSendingOperations.convertAndSend("/sub/errors/" + message.getMemberId(),
                BidErrorMessageDto.from("즉시구매가 이상으로 입찰할 수 없습니다."));

            throw new IllegalArgumentException("즉시구매가 이상으로 입찰할 수 없습니다.");
        }

        // 다른 경매의 최고입찰가로 현재 회원이 있는 경우의 총 금액
        long previousUsedPoint = getPreviousUsedPoint(message.getMemberId(), message, member);

        // 현재 보유 포인트에서 다른 경매에서 현재 회원이 최고입찰자인 경우 해당 포인트만큼 감소시킨 금액이 현재 입찰할 금액보다 적은 경우
        if (member.getPoint() - previousUsedPoint < message.getBidAmount()) {
            BidFailByPointMessageDto.Response response = BidFailByPointMessageDto.Response.from(
                previousUsedPoint, member.getPoint(),
                message.getBidAmount());

            // 해당 회원에게 에러 메시지 발송
            simpMessageSendingOperations.convertAndSend("/sub/errors/" + message.getMemberId(),
                response);

            throw new IllegalArgumentException("포인트 충전이 필요합니다.");
        }

        // 현재 입찰 시도한 금액 이상으로 먼저 입찰한 회원이 존재하는 경우
        if ((auction.getCurrentPrice() != auction.getStartPrice()) && (message.getBidAmount()
            < auction.getCurrentPrice() + 1000)) {
            BidFailByPreviousBidMessageDto.Response response = BidFailByPreviousBidMessageDto.Response.from(
                auction.getCurrentPrice(), message.getBidAmount());

            // 해당 회원에게 에러 메시지 발송
            simpMessageSendingOperations.convertAndSend("/sub/errors/" + message.getMemberId(),
                response);

            throw new IllegalArgumentException("먼저 입찰한 회원이 존재합니다.");
        }

        // 입찰 시도한 금액이 현재 입찰 시작가 금액보다 적은 경우
        if (message.getBidAmount() < auction.getStartPrice() + 1000) {

            // 해당 회원에게 에러 메시지 발송
            simpMessageSendingOperations.convertAndSend("/sub/errors/" + message.getMemberId(),
                BidErrorMessageDto.from("입찰 금액은 입찰 시작가보다 1000원 높은 금액부터 입찰할 수 있습니다."));

            throw new IllegalArgumentException(
                "입찰 금액은 입찰 시작가보다 1000원 높은 금액부터 입찰할 수 있습니다.");
        }
    }

    // 다른 경매의 최고입찰가로 현재 회원이 있는 경우의 총 금액구하는 메소드
    private long getPreviousUsedPoint(String memberId, BidMessageDto.Request message,
        Member member) {

        // todo: 입찰 때마다 회원이 참여중인 모든 경매를 불러와 최고입찰가 여부를 판단한다. 더 나은 방법 고민중
        List<Auction> auctionList = auctionRepository.findAllByMemberIdAndAuctionState(
            memberId, AuctionState.CONTINUE); // 회원이 참여중인 경매 리스트

        return auctionList.stream()
            .filter(a -> !a.getId().equals(message.getAuctionId())) // 현재 경매는 제외
            .map(a -> a.getBidList().stream()
                .max(Comparator.comparing(Bid::getBidPrice))
                .orElse(null)) // 경매의 최고 입찰가인 Bid, 없으면 null
            .filter(bid -> bid != null && bid.getMember().equals(member)) // 최고 입찰가의 회원이 현재 회원인 경우
            .mapToLong(Bid::getBidPrice)
            .sum(); // 해당 가격들을 더함
    }
}
