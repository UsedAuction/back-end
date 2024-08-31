package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.CONTINUE;
import static com.ddang.usedauction.auction.domain.AuctionState.END;
import static com.ddang.usedauction.notification.domain.NotificationType.QUESTION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.dto.AskCreateDto;
import com.ddang.usedauction.ask.repository.AskRepository;
import com.ddang.usedauction.ask.service.AskService;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
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
class NotificationServiceQuestionTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AskRepository askRepository;

    @InjectMocks
    private AskService askService;

    private Member seller;
    private Member buyer;
    private Auction auction;
    private AskCreateDto createDto;
    private Ask ask;

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

        auction = Auction.builder()
            .id(1L)
            .title("title")
            .auctionState(CONTINUE)
            .seller(seller)
            .build();

        createDto = AskCreateDto.builder()
            .title("문의 제목")
            .content("문의 내용")
            .auctionId(auction.getId())
            .build();

        ask = Ask.builder()
            .id(1L)
            .title("문의 제목")
            .content("문의 내용")
            .build();
    }

    @Test
    @DisplayName("구매자가 질문글 등록시 판매자에게 알림 전송 - 성공")
    void question_success() {

        //given
        given(auctionRepository.findById(createDto.getAuctionId())).willReturn(
            Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.of(buyer));
        given(askRepository.save(
            argThat(arg -> arg.getTitle().equals(createDto.getTitle()) &&
                arg.getContent().equals(createDto.getContent()))
        )).willReturn(ask);

        //when
        askService.createAsk(createDto, buyer.getMemberId());

        //then
        verify(notificationService, times(1))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매에 문의를 남겼습니다.",
                QUESTION
            );
    }

    @Test
    @DisplayName("구매자가 질문글 등록시 판매자에게 알림 전송 - 실패(존재하지 않는 경매)")
    void question_fail_1() {

        //given
        given(auctionRepository.findById(createDto.getAuctionId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> askService.createAsk(createDto, buyer.getEmail()));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매에 문의를 남겼습니다.",
                QUESTION
            );
    }

    @Test
    @DisplayName("구매자가 질문글 등록시 판매자에게 알림 전송 - 실패(존재하지 않는 구매자)")
    void question_fail_2() {

        //given
        given(auctionRepository.findById(createDto.getAuctionId())).willReturn(
            Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> askService.createAsk(createDto, buyer.getMemberId()));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매에 문의를 남겼습니다.",
                QUESTION
            );
    }

    @Test
    @DisplayName("구매자가 질문글 등록시 판매자에게 알림 전송 - 실패(종료된 경매)")
    void question_fail_3() {

        //given
        auction = Auction.builder()
            .id(1L)
            .auctionState(END)
            .seller(seller)
            .build();

        given(auctionRepository.findById(createDto.getAuctionId())).willReturn(
            Optional.of(auction));
        given(memberRepository.findByMemberId(buyer.getMemberId())).willReturn(Optional.of(buyer));

        //when
        assertThrows(IllegalStateException.class,
            () -> askService.createAsk(createDto, buyer.getMemberId()));

        //then
        verify(notificationService, times(0))
            .send(
                seller.getId(),
                auction.getId(),
                buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매에 문의를 남겼습니다.",
                QUESTION
            );
    }
}