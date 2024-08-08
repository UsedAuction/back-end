package com.ddang.usedauction.point.service;

import static com.ddang.usedauction.point.type.PointType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.dto.PointBalanceServiceDto;
import com.ddang.usedauction.point.dto.PointHistoryServiceDto;
import com.ddang.usedauction.point.exception.PointException;
import com.ddang.usedauction.point.repository.PointRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
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
        UserDetails userDetails = new User("test123@example.com", "123456", new ArrayList<>());

        Member member = Member.builder()
            .point(10000)
            .build();

        given(memberRepository.findByEmail("test123@example.com")).willReturn(Optional.of(member));

        // when
        PointBalanceServiceDto pointBalanceServiceDto = pointService.getPointBalance(userDetails);

        // then
        assertNotNull(pointBalanceServiceDto);
        assertEquals(10000, pointBalanceServiceDto.getPointAmount());
    }

    @Test
    @DisplayName("포인트 잔액 조회 - 실패(회원이 존재하지 않음)")
    void getPointBalanceFail_MemberNotFound() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123456", new ArrayList<>());
        given(memberRepository.findByEmail("test123@example.com")).willReturn(Optional.empty());

        // when
        // then
        assertThrows(MemberException.class, () -> pointService.getPointBalance(userDetails));
    }

    @Test
    @DisplayName("포인트 충전/사용 내역 조회 - 성공")
    void getPointListSuccess() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123456", new ArrayList<>());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Member member = Member.builder()
            .id(1L)
            .build();
        given(memberRepository.findByEmail("test123@example.com")).willReturn(Optional.of(member));

        List<PointHistory> pointHistoryList = new ArrayList<>();
        pointHistoryList.add(PointHistory.builder()
            .pointType(CHARGE)
            .pointAmount(5000)
            .curPointAmount(10000)
            .member(member)
            .build());
        pointHistoryList.add(PointHistory.builder()
            .pointType(USE)
            .pointAmount(3000)
            .curPointAmount(7000)
            .member(member)
            .build());

        Page<PointHistory> pointHistoryPage = new PageImpl<>(pointHistoryList, pageRequest, 2);
        given(pointRepository.findAllPoint("test123@example.com", startDate, endDate, pageRequest))
            .willReturn(pointHistoryPage);

        //when
        Page<PointHistoryServiceDto> result = pointService.getPointList(userDetails, startDate, endDate, pageRequest);

        //then
        assertNotNull(pointHistoryPage);
        assertEquals(2, result.getTotalElements());

        assertEquals(CHARGE, result.getContent().get(0).getPointType());
        assertEquals(5000, result.getContent().get(0).getPointAmount());
        assertEquals(10000, result.getContent().get(0).getCurPointAmount());
        assertEquals(1, result.getContent().get(0).getMemberId());

        assertEquals(USE, result.getContent().get(1).getPointType());
        assertEquals(3000, result.getContent().get(1).getPointAmount());
        assertEquals(7000, result.getContent().get(1).getCurPointAmount());
        assertEquals(1, result.getContent().get(1).getMemberId());
    }

    @Test
    @DisplayName("포인트 충전/사용 내역 조회 - 실패(종료일이 시작일보다 이전인 경우)")
    void getPointListFail_InvalidDateRange() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123456", new ArrayList<>());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(7);
        PageRequest pageRequest = PageRequest.of(0, 10);

        //when
        //then
        assertThrows(PointException.class, () ->
            pointService.getPointList(userDetails, startDate, endDate, pageRequest)
        );
    }

    @Test
    @DisplayName("포인트 충전/사용 내역 조회 - 실패(회원이 존재하지 않음)")
    void getPointListFail_MemberNotFound() {
        //given
        UserDetails userDetails = new User("test123@example.com", "123456", new ArrayList<>());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        when(memberRepository.findByEmail("test123@example.com")).thenReturn(Optional.empty());

        // then
        assertThrows(MemberException.class, () ->
            pointService.getPointList(userDetails, startDate, endDate, pageRequest)
        );
    }
}