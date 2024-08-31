package com.ddang.usedauction.ask.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.dto.AskCreateDto;
import com.ddang.usedauction.ask.dto.AskUpdateDto;
import com.ddang.usedauction.ask.repository.AskRepository;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.service.NotificationService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AskServiceTest {

    @Mock
    private AskRepository askRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AskService askService;

    Ask ask;

    @BeforeEach
    void setup() {

        ask = Ask.builder()
            .id(1L)
            .build();
    }

    @Test
    @DisplayName("문의 단건 조회")
    void getAsk() {

        when(askRepository.findById(1L)).thenReturn(Optional.of(ask));

        Ask result = askService.getAsk(1L);

        assertEquals(1, result.getId());
    }

    @Test
    @DisplayName("문의 단건 조회 실패 - 없는 문의")
    void getAskFail1() {

        when(askRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> askService.getAsk(1L));
    }

    @Test
    @DisplayName("회원이 작성한 문의 리스트 조회")
    void getAskList() {

        Pageable pageable = PageRequest.of(0, 10);

        Member writer = Member.builder()
            .email("test@naver.com")
            .memberId("test")
            .build();

        ask = ask.toBuilder()
            .writer(writer)
            .build();

        List<Ask> askList = List.of(ask);
        Page<Ask> askPageList = new PageImpl<>(askList, pageable, askList.size());

        when(askRepository.findAllByMemberId("test", pageable)).thenReturn(
            askPageList);

        Page<Ask> result = askService.getAskList("test", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getId());
    }

    @Test
    @DisplayName("문의 생성")
    void createAsk() {

        AskCreateDto createDto = AskCreateDto.builder()
            .title("title")
            .auctionId(1L)
            .content("content")
            .build();

        Member seller = Member.builder()
            .memberId("seller")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.CONTINUE)
            .seller(seller)
            .build();

        Member member = Member.builder()
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("memberId")).thenReturn(Optional.of(member));
        when(askRepository.save(argThat(arg -> arg.getTitle().equals("title")))).thenReturn(
            ask.toBuilder().title(createDto.getTitle()).writer(member)
                .content(createDto.getContent()).auction(auction).build());

        Ask result = askService.createAsk(createDto, "memberId");

        assertEquals("title", result.getTitle());
    }

    @Test
    @DisplayName("문의 생성 실패 - 없는 경매")
    void createAskFail1() {

        AskCreateDto createDto = AskCreateDto.builder()
            .title("title")
            .auctionId(1L)
            .content("content")
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> askService.createAsk(createDto, "test@naver.com"));
    }

    @Test
    @DisplayName("문의 생성 실패 - 없는 회원")
    void createAskFail2() {

        AskCreateDto createDto = AskCreateDto.builder()
            .title("title")
            .auctionId(1L)
            .content("content")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.CONTINUE)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("memberId")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> askService.createAsk(createDto, "memberId"));
    }

    @Test
    @DisplayName("문의 생성 실패 - 이미 경매가 종료된 경우")
    void createAskFail3() {

        AskCreateDto createDto = AskCreateDto.builder()
            .title("title")
            .auctionId(1L)
            .content("content")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.END)
            .build();

        Member member = Member.builder()
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("memberId")).thenReturn(Optional.of(member));

        assertThrows(IllegalStateException.class,
            () -> askService.createAsk(createDto, "memberId"));
    }

    @Test
    @DisplayName("문의 수정")
    void updateAsk() {

        AskUpdateDto updateDto = AskUpdateDto.builder()
            .content("content")
            .build();

        Member writer = Member.builder()
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        ask = ask.toBuilder()
            .writer(writer)
            .build();

        when(askRepository.findById(1L)).thenReturn(Optional.of(ask));
        when(askRepository.save(argThat(arg -> arg.getContent().equals("content")))).thenReturn(
            ask.toBuilder().content(
                updateDto.getContent()).build());

        Ask result = askService.updateAsk(1L, updateDto, "memberId");

        assertEquals("content", result.getContent());
    }

    @Test
    @DisplayName("문의 수정 실패 - 없는 문의")
    void updateAskFail1() {

        AskUpdateDto updateDto = AskUpdateDto.builder()
            .content("content")
            .build();

        when(askRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> askService.updateAsk(1L, updateDto, "test@naver.com"));
    }

    @Test
    @DisplayName("문의 수정 실패 - 작성자가 다른 경우")
    void updateAskFail2() {

        AskUpdateDto updateDto = AskUpdateDto.builder()
            .content("content")
            .build();

        Member writer = Member.builder()
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        ask = ask.toBuilder()
            .writer(writer)
            .build();

        when(askRepository.findById(1L)).thenReturn(Optional.of(ask));

        assertThrows(IllegalStateException.class,
            () -> askService.updateAsk(1L, updateDto, "memberI"));
    }

    @Test
    @DisplayName("회원이 작성한 문의 삭제")
    void deleteAsk() {

        Member writer = Member.builder()
            .email("test@naver.com")
            .build();

        ask = ask.toBuilder()
            .writer(writer)
            .build();

        when(askRepository.findByMemberId("test@naver.com")).thenReturn(Optional.of(ask));

        askService.deleteAsk("test@naver.com");

        verify(askRepository, times(1)).save(argThat(arg -> !arg.getDeletedAt().equals(null)));
    }

    @Test
    @DisplayName("회원이 작성한 문의 삭제 실패 - 없는 문의")
    void deleteAskFail1() {

        when(askRepository.findByMemberId("test@naver.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> askService.deleteAsk("test@naver.com"));
    }
}