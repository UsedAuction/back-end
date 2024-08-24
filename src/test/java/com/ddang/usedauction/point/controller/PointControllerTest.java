package com.ddang.usedauction.point.controller;

import static com.ddang.usedauction.point.type.PointType.CHARGE;
import static com.ddang.usedauction.point.type.PointType.USE;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.service.PointService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
@Disabled
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @Test
    @WithMockUser(username = "test123@example.com")
    @DisplayName("포인트 잔액 조회 - 성공")
    void getPointBalanceSuccess() throws Exception {
        //given
        long pointBalance = 10000L;
        UserDetails userDetails = new User("test123@example.com", "123qwe!@#", new ArrayList<>());

        given(pointService.getPointBalance(userDetails)).willReturn(pointBalance);

        //when
        //then
        mockMvc.perform(get("/api/members/points").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pointAmount").value(10000L));
    }

    @Test
    @DisplayName("포인트 잔액 조회 - 실패 (인증되지 않은 사용자)")
    void getPointBalanceFail_Unauthorized() throws Exception {
        //given
        SecurityContextHolder.clearContext();

        //when
        //then
        mockMvc.perform(get("/api/members/points").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test123@example.com")
    @DisplayName("포인트 충전/사용 내역 조회 - 성공")
    void getPointListSuccess() throws Exception {
        //given
        UserDetails userDetails = new User("test123@example.com", "123qwe!@#", new ArrayList<>());
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        Member member = Member.builder()
            .id(1L)
            .email(userDetails.getUsername())
            .build();

        Page<PointHistory> pointHistoryPage = new PageImpl<>(
            List.of(
                PointHistory.builder()
                    .id(1L)
                    .pointType(CHARGE)
                    .pointAmount(10000L)
                    .curPointAmount(10000L)
                    .member(member)
                    .build(),
                PointHistory.builder()
                    .id(2L)
                    .pointType(USE)
                    .pointAmount(-2000L)
                    .curPointAmount(8000L)
                    .member(member)
                    .build()),
            pageable,
            2
        );

        given(pointService.getPointList(userDetails, startDate, endDate, pageable)).willReturn(
            pointHistoryPage);

        //when
        //then
        mockMvc.perform(
                get("/api/members/points/history")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].pointType").value("CHARGE"))
            .andExpect(jsonPath("$.content[0].pointAmount").value(10000L))
            .andExpect(jsonPath("$.content[0].curPointAmount").value(10000L))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[1].pointType").value("USE"))
            .andExpect(jsonPath("$.content[1].pointAmount").value(-2000L))
            .andExpect(jsonPath("$.content[1].curPointAmount").value(8000L));
    }

    @Test
    @DisplayName("포인트 잔액 조회 - 실패 (인증되지 않은 사용자)")
    void getPointListFail_Unauthorized() throws Exception {
        //given
        SecurityContextHolder.clearContext();

        //when
        //then
        mockMvc.perform(get("/api/members/points/history").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
}