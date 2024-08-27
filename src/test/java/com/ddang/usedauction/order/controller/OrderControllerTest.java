package com.ddang.usedauction.order.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.dto.OrderCreateDto;
import com.ddang.usedauction.order.service.OrderService;
import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.security.jwt.Oauth2SuccessHandler;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({OrderController.class, SecurityConfig.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

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
    @DisplayName("주문 생성 - 성공")
    void createOrderSuccess() throws Exception {
        //given
        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(10000)
            .build();

        Member member = Member.builder()
            .id(1L)
            .email("test@naver.com")
            .build();

        Orders order = Orders.builder()
            .id(1L)
            .member(member)
            .itemName("10000 포인트")
            .price(10000)
            .tid(null)
            .build();

        given(orderService.createOrder(
            eq(member.getEmail()),
            argThat(arg -> arg.getPrice() == request.getPrice()))
        ).willReturn(order);

        //when
        //then
        mockMvc.perform(
            post("/api/members/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(1L))
            .andExpect(jsonPath("$.memberId").value(1L));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("주문 생성 - 실패 (상품가격 0)")
    void createOrderFail_PriceZero() throws Exception {
        //given
        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(0)
            .build();

        //when
        //then
        mockMvc.perform(
            post("/api/members/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0]").value("상품가격은 1이상이어야 합니다."));
    }

    @Test
    @WithCustomMockUser
    @DisplayName("주문 생성 - 실패 (상품가격 0 미만)")
    void createOrderFail_PriceNotPositive() throws Exception {
        //given
        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(-321)
            .build();

        //when
        //then
        mockMvc.perform(
            post("/api/members/orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0]").value("상품가격은 1이상이어야 합니다."));
    }
}