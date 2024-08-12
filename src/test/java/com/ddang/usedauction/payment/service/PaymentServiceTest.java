package com.ddang.usedauction.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.repository.OrderRepository;
import com.ddang.usedauction.payment.dto.Amount;
import com.ddang.usedauction.payment.dto.PaymentApproveDto;
import com.ddang.usedauction.payment.dto.PaymentInfoDto;
import com.ddang.usedauction.payment.dto.PaymentReadyDto;
import com.ddang.usedauction.payment.exception.PaymentReadyException;
import com.ddang.usedauction.point.repository.PointRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private PaymentService paymentService;

    private final String SECRET_KEY = "secret-key";
    private final String READY_URL = "ready-url";
    private final String APPROVE_URL = "approve-url";

    private Member member;
    private Orders readyOrder;

    private PaymentInfoDto.Request request;
    private PaymentReadyDto.Response readyResponse;
    private HttpEntity<Map<String, String>> readyRequestEntity;

    private Orders approveOrder;
    private String partnerOrderId;
    private String pgToken;
    private PaymentApproveDto.Response approveResponse;
    private HttpEntity<Map<String, String>> approveRequestEntity;

    @BeforeEach
    void before() {

        ReflectionTestUtils.setField(paymentService, "SECRET_KEY", SECRET_KEY);
        ReflectionTestUtils.setField(paymentService, "READY_URL", READY_URL);
        ReflectionTestUtils.setField(paymentService, "APPROVE_URL", APPROVE_URL);

        member = Member.builder()
            .id(1L)
            .email("test123@example.com")
            .point(10000L)
            .build();

        readyOrder = Orders.builder()
            .id(1L)
            .member(member)
            .itemName("10000 포인트")
            .price(10000)
            .tid(null)
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + SECRET_KEY);
        headers.set("Content-Type", "application/json");

        // 결제 준비
        request = PaymentInfoDto.Request.builder()
            .orderId(1L)
            .memberId(1L)
            .price(10000)
            .build();

        Map<String, String> readyMap = Map.of(
            "cid", "TC0ONETIME",
            "partner_order_id", "1",
            "partner_user_id", "1",
            "item_name", "10000 포인트",
            "quantity", "1",
            "total_amount", "10000",
            "tax_free_amount", "0",
            "approval_url", "http://localhost:8080/api/members/payment/approve?partner_order_id=1",
            "cancel_url", "http://localhost:8080/api/members/payment/cancel",
            "fail_url", "http://localhost:8080/api/members/payment/fail"
        );

        readyRequestEntity = new HttpEntity<>(readyMap, headers);

        readyResponse = PaymentReadyDto.Response.builder()
            .tid("T6b8m123257c75a88q63")
            .next_redirect_pc_url("https://online-pay.kakao.com/mockup/v1/g12b1c3552f2f80eda200a29t3b72e45057f8e528843ebb0a80d0235ceb9d7e3/info")
            .created_at(LocalDateTime.parse("2024-08-11T21:58:43", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .build();

        // 결제 승인
        partnerOrderId = "1";
        pgToken = "bzb52391ee335e521f3f";

        approveOrder = Orders.builder()
            .id(1L)
            .member(member)
            .itemName("10000 포인트")
            .price(10000)
            .tid("T6b8m123257c75a88q63")
            .build();

        Map<String, String> approveMap = Map.of(
            "cid", "TC0ONETIME",
            "tid", "T6b8m123257c75a88q63",
            "partner_order_id", "1",
            "partner_user_id", "1",
            "pg_token", "bzb52391ee335e521f3f"
        );

        approveRequestEntity = new HttpEntity<>(approveMap, headers);

        approveResponse = PaymentApproveDto.Response.builder()
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
    }

    @Test
    @DisplayName("결제 준비 - 성공")
    void readySuccess() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(orderRepository.findById(readyOrder.getId())).willReturn(Optional.of(readyOrder));
        given(restTemplate.postForObject(READY_URL, readyRequestEntity, PaymentReadyDto.Response.class))
            .willReturn(readyResponse);

        //when
        PaymentReadyDto.Response response = paymentService.ready(request);

        //then
        assertNotNull(response);
        assertEquals(readyResponse.getTid(), response.getTid());
        assertEquals(readyResponse.getNext_redirect_pc_url(), response.getNext_redirect_pc_url());
        assertEquals(readyResponse.getCreated_at(), response.getCreated_at());

        verify(orderRepository).save(argThat(arg -> "T6b8m123257c75a88q63".equals(arg.getTid())));
    }

    @Test
    @DisplayName("결제 준비 - 실패 (회원이 존재하지 않음)")
    void readyFail_MemberNotFound() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class, () -> paymentService.ready(request));
    }

    @Test
    @DisplayName("결제 준비 - 실패 (주문내역이 존재하지 않음)")
    void readyFail_OrderNotFound() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(orderRepository.findById(request.getOrderId())).willReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class, () -> paymentService.ready(request));
    }

    @Test
    @DisplayName("결제 준비 - 실패 (회원 id가 일치하지 않음)")
    void readyFail_MemberNotEqual() {
        //given
        PaymentInfoDto.Request request = PaymentInfoDto.Request.builder()
            .orderId(1L)
            .memberId(2L)
            .price(10000)
            .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(orderRepository.findById(request.getOrderId())).willReturn(Optional.of(readyOrder));

        //when
        //then
        assertThrows(IllegalArgumentException.class, () -> paymentService.ready(request));
    }

    @Test
    @DisplayName("결제 준비 - 실패 (금액 불일치)")
    void readyFail_PriceNotEqual() {
        //given
        PaymentInfoDto.Request request = PaymentInfoDto.Request.builder()
            .orderId(1L)
            .memberId(1L)
            .price(20000)
            .build();

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(orderRepository.findById(request.getOrderId())).willReturn(Optional.of(readyOrder));

        //when
        //then
        assertThrows(IllegalArgumentException.class, () -> paymentService.ready(request));
    }

    @Test
    @DisplayName("결제 준비 - 실패 (결제 준비 요청에 대한 응답이 없음)")
    void readyFail_apiFailed() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(orderRepository.findById(request.getOrderId())).willReturn(Optional.of(readyOrder));
        given(restTemplate.postForObject(READY_URL, readyRequestEntity, PaymentReadyDto.Response.class))
            .willThrow(new PaymentReadyException("결제 준비 요청에 대한 응답이 없습니다."));

        //when
        //then
        assertThrows(PaymentReadyException.class, () -> paymentService.ready(request));
    }

    @Test
    @DisplayName("결제 승인 - 성공")
    void approveSuccess() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(orderRepository.findById(approveOrder.getId())).willReturn(Optional.of(approveOrder));
        given(restTemplate.postForObject(
            APPROVE_URL, approveRequestEntity, PaymentApproveDto.Response.class))
            .willReturn(approveResponse);

        //when
        PaymentApproveDto.Response response = paymentService.approve(partnerOrderId, pgToken);

        //then
        assertNotNull(response);
        assertEquals(approveResponse.getAid(), response.getAid());
        assertEquals(approveResponse.getTid(), response.getTid());
        assertEquals(approveResponse.getCid(), response.getCid());
        assertEquals(approveResponse.getPartner_order_id(), response.getPartner_order_id());
        assertEquals(approveResponse.getPartner_user_id(), response.getPartner_user_id());
        assertEquals(approveResponse.getPayment_method_type(), response.getPayment_method_type());
        assertEquals(approveResponse.getAmount(), response.getAmount());
        assertEquals(approveResponse.getItem_name(), response.getItem_name());
        assertEquals(approveResponse.getQuantity(), response.getQuantity());
        assertEquals(approveResponse.getCreated_at(), response.getCreated_at());
        assertEquals(approveResponse.getApproved_at(), response.getApproved_at());

        verify(memberRepository).save(argThat(arg -> arg.getPoint() == (10000 + 10000)));
        verify(pointRepository).save(argThat(arg -> arg.getPointAmount() == 10000));
    }

    @Test
    @DisplayName("결제 승인 - 실패 (회원이 존재하지 않음)")
    void approveFail_MemberNotFound() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class, () -> paymentService.approve(partnerOrderId, pgToken));
    }

    @Test
    @DisplayName("결제 승인 - 실패 (주문내역이 존재하지 않음)")
    void approveFail_OrderNotFound() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(orderRepository.findById(Long.valueOf(partnerOrderId))).willReturn(Optional.empty());

        //when
        //then
        assertThrows(EntityNotFoundException.class, () -> paymentService.approve(partnerOrderId, pgToken));
    }

    @Test
    @DisplayName("결제 승인 - 실패 (결제 승인 요청에 대한 응답이 없음)")
    void approveFail_apiFailed() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(orderRepository.findById(Long.valueOf(partnerOrderId))).willReturn(Optional.of(approveOrder));
        given(restTemplate.postForObject(APPROVE_URL, approveRequestEntity, PaymentApproveDto.Response.class))
            .willThrow(new PaymentReadyException("결제 준비 요청에 대한 응답이 없습니다."));

        //when
        //then
        assertThrows(PaymentReadyException.class, () -> paymentService.approve(partnerOrderId, pgToken));
    }
}