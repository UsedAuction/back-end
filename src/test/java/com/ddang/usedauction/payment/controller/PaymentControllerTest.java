package com.ddang.usedauction.payment.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.payment.dto.Amount;
import com.ddang.usedauction.payment.dto.PaymentApproveDto;
import com.ddang.usedauction.payment.dto.PaymentInfoDto;
import com.ddang.usedauction.payment.dto.PaymentReadyDto;
import com.ddang.usedauction.payment.exception.PaymentApproveException;
import com.ddang.usedauction.payment.exception.PaymentReadyException;
import com.ddang.usedauction.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 준비 - 성공")
    void paymentReadySuccess() throws Exception {
        //given
        PaymentInfoDto.Request request = PaymentInfoDto.Request.builder()
            .orderId(1L)
            .memberId(1L)
            .price(10000)
            .build();

        PaymentReadyDto.Response response = PaymentReadyDto.Response.builder()
            .tid("T6b8839e257c75a59c1c")
            .next_redirect_pc_url(
                "https://online-pay.kakao.com/mockup/v1/36fa5788ed0e2973f66764a4faef948248e5e094dc5a4c7a581e8217fd83ac6e/info")
            .created_at(LocalDateTime.parse("2024-08-11T18:25:50", DateTimeFormatter.ISO_DATE_TIME))
            .build();

        given(paymentService.ready(argThat(arg ->
            arg.getOrderId().equals(request.getOrderId()) &&
                arg.getMemberId().equals(request.getMemberId()) &&
                arg.getPrice() == (request.getPrice())
        ))).willReturn(response);

        //when
        //then
        mockMvc.perform(
                post("/api/members/payment/ready")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tid").value("T6b8839e257c75a59c1c"))
            .andExpect(jsonPath("$.next_redirect_pc_url").value(
                "https://online-pay.kakao.com/mockup/v1/36fa5788ed0e2973f66764a4faef948248e5e094dc5a4c7a581e8217fd83ac6e/info"))
            .andExpect(jsonPath("$.created_at").value("2024-08-11T18:25:50"));
    }

    @Test
    @DisplayName("결제 준비 - 실패 (price가 0)")
    void paymentReadyFail_PriceZero() throws Exception {
        //given
        PaymentInfoDto.Request request = PaymentInfoDto.Request.builder()
            .orderId(1L)
            .memberId(1L)
            .price(0)
            .build();

        //when
        //then
        mockMvc.perform(
                post("/api/members/payment/ready")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("[\"상품가격은 1 이상이어야 합니다.\"]"));
    }

    @Test
    @DisplayName("결제 준비 - 실패 (price가 0 미만)")
    void paymentReadyFail_PriceMinus() throws Exception {
        //given
        PaymentInfoDto.Request request = PaymentInfoDto.Request.builder()
            .orderId(1L)
            .memberId(1L)
            .price(-1234)
            .build();

        //when
        //then
        mockMvc.perform(
                post("/api/members/payment/ready")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("[\"상품가격은 1 이상이어야 합니다.\"]"));
    }

    @Test
    @DisplayName("결제 준비 - 실패 (결제 준비 요청에 대한 응답 없음)")
    void paymentReadyFail_ApiNoResponse() throws Exception {
        // given
        PaymentInfoDto.Request request = PaymentInfoDto.Request.builder()
            .orderId(1L)
            .memberId(1L)
            .price(10000)
            .build();

        given(paymentService.ready(argThat(arg ->
            arg.getOrderId().equals(request.getOrderId()) &&
                arg.getMemberId().equals(request.getMemberId()) &&
                arg.getPrice() == (request.getPrice())
        ))).willThrow(new PaymentReadyException("결제 준비 요청에 대한 응답이 없습니다."));

        // when
        // then
        mockMvc.perform(
                post("/api/members/payment/ready")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().string("결제 준비 요청에 대한 응답이 없습니다."));
    }

    @Test
    @DisplayName("결제 승인 - 성공")
    void paymentApproveSuccess() throws Exception {
        //given
        String partnerOrderId = "1";
        String pgToken = "bzb52391ee335e521f3f";

        PaymentApproveDto.Response response = PaymentApproveDto.Response.builder()
            .aid("A6z909b238884dbbed6e")
            .tid("T6b8m123257c75a88q63")
            .cid("TC0ONETIME")
            .partner_order_id("1")
            .partner_user_id("1")
            .payment_method_type("MONEY")
            .amount(Amount.builder()
                .total(10000)
                .tax_free(0)
                .build()
            )
            .item_name("10000 포인트")
            .quantity(1)
            .created_at(LocalDateTime.parse("2024-08-11T17:26:13", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .approved_at(LocalDateTime.parse("2024-08-11T17:26:49", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .build();

        given(paymentService.approve(partnerOrderId, pgToken)).willReturn(response);

        //when
        //then
        mockMvc.perform(
                get("/api/members/payment/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("partner_order_id", partnerOrderId)
                    .param("pg_token", pgToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.aid").value("A6z909b238884dbbed6e"))
            .andExpect(jsonPath("$.tid").value("T6b8m123257c75a88q63"))
            .andExpect(jsonPath("$.cid").value("TC0ONETIME"))
            .andExpect(jsonPath("$.partner_order_id").value("1"))
            .andExpect(jsonPath("$.partner_user_id").value("1"))
            .andExpect(jsonPath("$.payment_method_type").value("MONEY"))
            .andExpect(jsonPath("$.amount.total").value(10000))
            .andExpect(jsonPath("$.item_name").value("10000 포인트"))
            .andExpect(jsonPath("$.quantity").value(1))
            .andExpect(jsonPath("$.created_at").value("2024-08-11T17:26:13"))
            .andExpect(jsonPath("$.approved_at").value("2024-08-11T17:26:49"));
    }

    @Test
    @DisplayName("결제 승인 - 실패 (결제 승인 요청에 대한 응답 없음)")
    void paymentApproveFail_ApiNoResponse() throws Exception {
        // given
        String partnerOrderId = "1";
        String pgToken = "bzb52391ee335e521f3f";

        given(paymentService.approve(partnerOrderId, pgToken))
            .willThrow(new PaymentApproveException("결제 승인 요청에 대한 응답이 없습니다."));

        // when
        // then
        mockMvc.perform(
                get("/api/members/payment/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("partner_order_id", partnerOrderId)
                    .param("pg_token", pgToken))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().string("결제 승인 요청에 대한 응답이 없습니다."));
    }

    @Test
    @DisplayName("결제 취소 - 성공")
    void paymentCancelSuccess() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(
                get("/api/members/payment/cancel")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("결제를 취소했습니다."));
    }

    @Test
    @DisplayName("결제 실패 - 성공")
    void paymentFailSuccess() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(
                get("/api/members/payment/fail")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("결제가 실패되었습니다."));
    }
}