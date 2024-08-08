package com.ddang.usedauction.payment.service;

import static com.ddang.usedauction.order.exception.OrderErrorCode.NOT_FOUND_ORDER;
import static com.ddang.usedauction.payment.exception.PaymentErrorCode.INVALID_USER;
import static com.ddang.usedauction.payment.exception.PaymentErrorCode.NOT_EQUAL_PAYMENT_AMOUNT;
import static com.ddang.usedauction.payment.exception.PaymentErrorCode.PAYMENT_FAIL;
import static com.ddang.usedauction.point.type.PointType.CHARGE;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.exception.MemberErrorCode;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.exception.OrderErrorCode;
import com.ddang.usedauction.order.exception.OrderException;
import com.ddang.usedauction.order.repository.OrderRepository;
import com.ddang.usedauction.payment.dto.PaymentApproveDto;
import com.ddang.usedauction.payment.dto.PaymentInfoDto;
import com.ddang.usedauction.payment.dto.PaymentReadyDto;
import com.ddang.usedauction.payment.exception.PaymentException;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import com.ddang.usedauction.point.type.PointType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    @Value("${payment.secret-key}")
    private String SECRET_KEY;

    @Value("${payment.ready}")
    private String READY_URL;

    static final String CID = "TC0ONETIME";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;

    // 결제 준비
    public PaymentReadyDto.Response ready (PaymentInfoDto.Request request) {
//         아래 코드는 테스트 용이성을 위해 주석처리. 회원기능 구현 완료시 수정 예정
//         (UserDetails userDetails, PaymentReadyDto.Request request) {

//        String email = userDetails.getUsername();
//        Member member = memberRepository.findByEmail(email)
//            .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        Member member = Member.builder()
            .id(1L)
            .build();

        Orders order = orderRepository.findByMemberId(member.getId())
            .orElseThrow(() -> new OrderException(NOT_FOUND_ORDER));

        // 로그인한 유저와 요청으로 받은 유저의 id가 동일한지 비교
        if (!member.getId().equals(request.getMemberId())) {
            throw new PaymentException(INVALID_USER);
        }

        // db에 저장된 금액과 입력받은 금액이 동일한지 비교
        if (order.getPrice() != request.getPrice()) {
            throw new PaymentException(NOT_EQUAL_PAYMENT_AMOUNT);
        }

        // 요청으로 받은 값들을 String으로 변환
        String orderIdStr = String.valueOf(request.getOrderId());
        String memberIdStr = String.valueOf(member.getId());
        String itemNameStr = request.getPrice() + " 포인트"; // 포인트아이템을 db에 저장하지 않으므로 포인트값으로 아이템명을 생성함
        String priceStr = String.valueOf(request.getPrice());

        // 카카오로 보낼 결제 준비 요청에 필요한 정보들 생성
        // header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_AUTHORIZATION, "SECRET_KEY " + SECRET_KEY);
        headers.set(HEADER_CONTENT_TYPE, "application/json");

        // body 설정
        PaymentReadyDto.Request paymentRequest = PaymentReadyDto.Request.builder()
            .cid(CID)
            .partnerOrderId(String.valueOf(orderIdStr))
            .partnerUserId(String.valueOf(memberIdStr))
            .itemName(itemNameStr)
            .quantity("1")
            .totalAmount(priceStr)
            .taxFreeAmount("0")
            .approvalUrl("http://localhost:8080/payment/success?partner_order_id=" + orderIdStr)
            .cancelUrl("http://localhost:8080/payment/cancel")
            .failUrl("http://localhost:8080/payment/fail")
            .build();

        // paymentRequest를 map으로 변환
        Map<String, String> map = paymentRequest.toMap();

        // header, body 하나로 합치기
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(map, headers);

        // restTemplate.postForObject(요청 보낼 url, 헤더+바디, 응답을 매핑할 dto)
        // postForObject: post 요청을 보내고 응답받은 json을 java 객체로 변환
        PaymentReadyDto.Response response = restTemplate.postForObject(
            READY_URL, requestEntity, PaymentReadyDto.Response.class
        );

        log.info("response: {}", response);

        // order 테이블에 tid 저장
        order.setTid(response.getTid());
        orderRepository.save(order);

        return response;
    }
}
