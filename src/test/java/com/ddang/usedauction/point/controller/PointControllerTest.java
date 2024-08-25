package com.ddang.usedauction.point.controller;

import static com.ddang.usedauction.point.domain.PointType.CHARGE;
import static com.ddang.usedauction.point.domain.PointType.USE;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.service.PointService;
import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.security.jwt.Oauth2SuccessHandler;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.service.RefreshTokenService;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({PointController.class, SecurityConfig.class})
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private PrincipalOauth2UserService principalOauth2UserService;

    @MockBean
    private Oauth2SuccessHandler oauth2SuccessHandler;

    @MockBean
    private Oauth2FailureHandler oauth2FailureHandler;

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 잔액 조회 - 성공")
    void getPointBalanceSuccess() throws Exception {
        //given
        long pointBalance = 10000L;

        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .build();

        given(pointService.getPointBalance(member.getEmail())).willReturn(pointBalance);

        //when
        //then
        mockMvc.perform(
            get("/api/members/points")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pointAmount").value(10000L));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 잔액 조회 - 실패 (인증되지 않은 사용자)")
    void getPointBalanceFail_1() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(
            get("/api/members/points")
                .contentType(MediaType.APPLICATION_JSON)
                .with(anonymous()))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("포인트 충전/사용 내역 조회 - 성공")
    void getPointListSuccess() throws Exception {
        //given
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now();
        String sorted = "latest";
        Pageable pageable = PageRequest.of(0, 10);

        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .build();

        PointHistory pointHistory1 = PointHistory.builder()
            .id(1L)
            .pointType(CHARGE)
            .pointAmount(10000L)
            .curPointAmount(10000L)
            .member(member)
            .build();

        PointHistory pointHistory2 = PointHistory.builder()
            .id(2L)
            .pointType(USE)
            .pointAmount(-2000L)
            .curPointAmount(8000L)
            .member(member)
            .build();

        Page<PointHistory> pointHistoryPage =
            new PageImpl<>(List.of(pointHistory1, pointHistory2), pageable, 2);

        given(pointService.getPointList(member.getEmail(), startDate, endDate, sorted, pageable))
            .willReturn(pointHistoryPage);

        //when
        //then
        mockMvc.perform(
            get("/api/members/points/history")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("sorted", sorted)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
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
    @WithCustomMockUser
    @DisplayName("포인트 잔액 조회 - 실패 (인증되지 않은 사용자)")
    void getPointListFail_1() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(
            get("/api/members/points/history")
                .contentType(MediaType.APPLICATION_JSON)
                .with(anonymous()))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}