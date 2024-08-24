package com.ddang.usedauction.point.service;

import static com.ddang.usedauction.point.type.PointType.CHARGE;
import static com.ddang.usedauction.point.type.PointType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
@Disabled
class PointServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("포인트 잔액 조회 - 성공")
    void getPointBalanceSuccess() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123qwe!@#", new ArrayList<>());
        Member member = Member.builder()
            .email(userDetails.getUsername())
            .point(10000L)
            .build();

        given(memberRepository.findByEmail(userDetails.getUsername())).willReturn(
            Optional.of(member));

        // when
        long pointBalance = pointService.getPointBalance(userDetails);

        // then
        assertEquals(10000L, pointBalance);
    }

    @Test
    @DisplayName("포인트 잔액 조회 - 실패(회원이 존재하지 않음)")
    void getPointBalanceFail_MemberNotFound() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123qwe!@#", new ArrayList<>());

        given(memberRepository.findByEmail(userDetails.getUsername())).willReturn(Optional.empty());

        // when
        // then
        assertThrows(NoSuchElementException.class, () -> pointService.getPointBalance(userDetails));
    }

    @Test
    @DisplayName("포인트 충전/사용 내역 조회 - 성공")
    void getPointListSuccess() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123qwe!@#", new ArrayList<>());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Member member = Member.builder()
            .id(1L)
            .email(userDetails.getUsername())
            .build();

        given(memberRepository.findByEmail(userDetails.getUsername())).willReturn(
            Optional.of(member));

        List<PointHistory> pointHistoryList = new ArrayList<>();
        pointHistoryList.add(PointHistory.builder()
            .pointType(CHARGE)
            .pointAmount(5000L)
            .curPointAmount(10000L)
            .member(member)
            .build());
        pointHistoryList.add(PointHistory.builder()
            .pointType(USE)
            .pointAmount(3000L)
            .curPointAmount(7000L)
            .member(member)
            .build());

        given(pointRepository.findAllPoint(userDetails.getUsername(), startDate, endDate,
            pageRequest))
            .willReturn(new PageImpl<>(pointHistoryList, pageRequest, 2));

        //when
        Page<PointHistory> pointHistoryPage = pointService.getPointList(userDetails, startDate,
            endDate, pageRequest);

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
    @DisplayName("포인트 충전/사용 내역 조회 - 실패(종료일이 시작일보다 이전인 경우)")
    void getPointListFail_InvalidDateRange() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123qwe!@#", new ArrayList<>());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);
        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        //then
        assertThrows(IllegalArgumentException.class, () ->
            pointService.getPointList(userDetails, startDate, endDate, pageRequest)
        );
    }

    @Test
    @DisplayName("포인트 충전/사용 내역 조회 - 실패(회원이 존재하지 않음)")
    void getPointListFail_MemberNotFound() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123qwe!@#", new ArrayList<>());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        when(memberRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.empty());

        // then
        assertThrows(NoSuchElementException.class, () ->
            pointService.getPointList(userDetails, startDate, endDate, pageRequest)
        );
    }
}