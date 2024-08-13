package com.ddang.usedauction.order.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.dto.OrderCreateDto;
import com.ddang.usedauction.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 - 성공")
    void createOrderSuccess() throws Exception {
        //given
        OrderCreateDto.Request request = OrderCreateDto.Request.builder()
            .price(10000)
            .build();

        Member member = Member.builder()
            .id(1L)
            .build();

        Orders order = Orders.builder()
            .id(1L)
            .member(member)
            .itemName("10000 포인트")
            .price(10000)
            .tid(null)
            .build();

        given(orderService.createOrder(argThat(arg ->
            arg.getPrice() == order.getPrice()))).willReturn(order);

        //when
        //then
        mockMvc.perform(
                post("/api/members/orders/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value(1L))
            .andExpect(jsonPath("$.memberId").value(1L));
    }

    @Test
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
                    .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0]").value("상품가격은 1이상이어야 합니다."));
    }

    @Test
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