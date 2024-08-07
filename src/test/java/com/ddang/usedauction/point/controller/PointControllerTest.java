package com.ddang.usedauction.point.controller;

import static com.ddang.usedauction.member.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.ddang.usedauction.point.type.PointType.CHARGE;
import static com.ddang.usedauction.point.type.PointType.USE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.member.MemberException;
import com.ddang.usedauction.point.dto.PointBalanceDto;
import com.ddang.usedauction.point.dto.PointHistoryDto;
import com.ddang.usedauction.point.service.PointService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PointController.class)
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
        PointBalanceDto pointBalanceDto = new PointBalanceDto(10000);

        //when
        when(pointService.getPointBalance(any(UserDetails.class)))
            .thenReturn(pointBalanceDto);

        //then
        mockMvc.perform(get("/api/members/points").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("성공"))
            .andExpect(jsonPath("$.data.pointAmount").value(10000));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("포인트 잔액 조회 - 실패(회원이 존재하지 않음)")
    void getPointBalanceFail() throws Exception {
        // given
        // when
        when(pointService.getPointBalance(any(UserDetails.class)))
            .thenThrow(new MemberException(MEMBER_NOT_FOUND));

        // then
        mockMvc.perform(get("/api/members/points").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.message").value("회원이 존재하지 않습니다."))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("포인트 충전/사용 내역 조회 - 성공")
    void getPointListSuccess() throws Exception {
        //given
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<PointHistoryDto> pointHistoryDtoPage = new PageImpl<>(
            List.of(
                PointHistoryDto.builder()
                    .id(1L)
                    .pointType(CHARGE)
                    .pointAmount(10000)
                    .curPointAmount(10000)
                    .memberId(1L)
                    .build(),
                PointHistoryDto.builder()
                    .id(2L)
                    .pointType(USE)
                    .pointAmount(-2000)
                    .curPointAmount(8000)
                    .memberId(1L)
                    .build()),
            pageable,
            2
        );

        //when
        when(pointService.getPointList(any(UserDetails.class), any(LocalDate.class), any(LocalDate.class),
            any(Pageable.class)))
            .thenReturn(pointHistoryDtoPage);

        //then
        mockMvc.perform(get("/api/members/points/history")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
            .andExpect(jsonPath("$.message").value("성공"))
            .andExpect(jsonPath("$.data.content[0].id").value(1))
            .andExpect(jsonPath("$.data.content[0].pointType").value("CHARGE"))
            .andExpect(jsonPath("$.data.content[0].pointAmount").value(10000))
            .andExpect(jsonPath("$.data.content[0].curPointAmount").value(10000))
            .andExpect(jsonPath("$.data.content[1].id").value(2))
            .andExpect(jsonPath("$.data.content[1].pointType").value("USE"))
            .andExpect(jsonPath("$.data.content[1].pointAmount").value(-2000))
            .andExpect(jsonPath("$.data.content[1].curPointAmount").value(8000));
    }
}