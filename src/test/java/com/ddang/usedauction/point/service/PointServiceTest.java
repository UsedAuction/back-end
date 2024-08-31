package com.ddang.usedauction.point.service;

import static com.ddang.usedauction.point.domain.PointType.CHARGE;
import static com.ddang.usedauction.point.domain.PointType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 잔액 조회 - 성공")
    void getPointBalanceSuccess() {
        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .memberId("memberId")
            .point(10000L)
            .build();

        given(memberRepository.findByMemberId(member.getMemberId())).willReturn(
            Optional.of(member));

        // when
        long pointBalance = pointService.getPointBalance(member.getMemberId());

        // then
        assertEquals(10000L, pointBalance);
    }

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 잔액 조회 - 실패(회원이 존재하지 않음)")
    void getPointBalanceFail_MemberNotFound() {
        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        given(memberRepository.findByMemberId(member.getMemberId())).willReturn(Optional.empty());

        // when
        // then
        assertThrows(NoSuchElementException.class,
            () -> pointService.getPointBalance(member.getMemberId()));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 충전/사용 내역 조회 - 성공")
    void getPointListSuccess() {
        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .memberId("memberId")
            .point(10000L)
            .build();

        String sorted = "latest";

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(0, 10);

        Sort sort = Sort.by(Direction.DESC, "createdAt");
        Pageable sortPage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        List<PointHistory> pointHistoryList = List.of(
            PointHistory.builder()
                .pointType(CHARGE)
                .pointAmount(5000L)
                .curPointAmount(10000L)
                .member(member)
                .build(),
            PointHistory.builder()
                .pointType(USE)
                .pointAmount(3000L)
                .curPointAmount(7000L)
                .member(member)
                .build()
        );

        given(memberRepository.findByMemberId(member.getMemberId())).willReturn(
            Optional.of(member));
        given(pointRepository.findByMemberMemberIdAndCreatedAtBetween(member.getMemberId(),
            startDateTime, endDateTime, sortPage))
            .willReturn(new PageImpl<>(pointHistoryList, sortPage, pointHistoryList.size()));

        //when
        Page<PointHistory> pointHistoryPage =
            pointService.getPointList(member.getMemberId(), startDate, endDate, sorted, pageable);

        //then
        assertNotNull(pointHistoryPage);
        assertEquals(2, pointHistoryPage.getTotalElements());

        assertEquals(CHARGE, pointHistoryPage.getContent().get(0).getPointType());
        assertEquals(5000L, pointHistoryPage.getContent().get(0).getPointAmount());
        assertEquals(10000L, pointHistoryPage.getContent().get(0).getCurPointAmount());
        assertEquals(1, pointHistoryPage.getContent().get(0).getMember().getId());

        assertEquals(USE, pointHistoryPage.getContent().get(1).getPointType());
        assertEquals(3000L, pointHistoryPage.getContent().get(1).getPointAmount());
        assertEquals(7000L, pointHistoryPage.getContent().get(1).getCurPointAmount());
        assertEquals(1, pointHistoryPage.getContent().get(1).getMember().getId());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 충전/사용 내역 조회 - 실패(종료일이 시작일보다 이전인 경우)")
    void getPointListFail_1() {
        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .point(10000L)
            .build();

        String sorted = "latest";

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);

        Pageable pageable = PageRequest.of(0, 10);

        //when
        //then
        assertThrows(IllegalArgumentException.class, () ->
            pointService.getPointList(member.getEmail(), startDate, endDate, sorted, pageable)
        );
    }

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 충전/사용 내역 조회 - 실패(회원이 존재하지 않음)")
    void getPointListFail_2() {
        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .memberId("memberId")
            .point(10000L)
            .build();

        String sorted = "latest";

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(Optional.empty());

        // then
        assertThrows(NoSuchElementException.class, () ->
            pointService.getPointList(member.getMemberId(), startDate, endDate, sorted, pageable)
        );
    }

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 충전/사용 내역 조회 - 실패(잘못된 정렬값)")
    void getPointListFail_3() {
        //given
        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .memberId("memberId")
            .point(10000L)
            .build();

        String sorted = "qwer";

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        when(memberRepository.findByMemberId(member.getMemberId())).thenReturn(Optional.of(member));

        // then
        assertThrows(IllegalArgumentException.class, () ->
            pointService.getPointList(member.getMemberId(), startDate, endDate, sorted, pageable)
        );
    }
}