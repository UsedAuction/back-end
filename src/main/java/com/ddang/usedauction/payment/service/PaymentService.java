package com.ddang.usedauction.payment.service;

import static com.ddang.usedauction.point.type.PointType.CHARGE;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.repository.OrderRepository;
import com.ddang.usedauction.payment.dto.PaymentApproveDto;
import com.ddang.usedauction.payment.dto.PaymentInfoDto;
import com.ddang.usedauction.payment.dto.PaymentReadyDto;
import com.ddang.usedauction.payment.exception.PaymentReadyException;
import com.ddang.usedauction.payment.exception.PaymentApproveException;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
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

    @Value("${payment.approve}")
    private String APPROVE_URL;

    static final String CID = "TC0ONETIME";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;

    // 결제 준비
    public PaymentReadyDto.Response ready (PaymentInfoDto.Request request) {

        Member member = memberRepository.findById(1L)
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다.")); // TODO 토큰을 받아 처리하는 것으로 수정

        Orders order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new EntityNotFoundException("주문내역이 존재하지 않습니다."));

        // 로그인한 유저와 요청으로 받은 유저의 id가 동일한지 비교
        if (!member.getId().equals(request.getMemberId())) {
            throw new IllegalArgumentException("동일한 유저가 아닙니다.");
        }

        // db에 저장된 금액과 입력받은 금액이 동일한지 비교
        if (order.getPrice() != request.getPrice()) {
            throw new IllegalArgumentException("요청 금액과 저장된 금액이 일치하지 않습니다.");
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
            .approvalUrl("http://localhost:8080/api/members/payment/approve?partner_order_id=" + orderIdStr)
            .cancelUrl("http://localhost:8080/api/members/payment/cancel")
            .failUrl("http://localhost:8080/api/members/payment/fail")
            .build();

        // paymentRequest를 map으로 변환
        Map<String, String> map = paymentRequest.toMap();

        // header, body 하나로 합치기
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(map, headers);

        // 결제 준비 요청하기
        // postForObject(요청 보낼 url, 헤더+바디, 응답을 매핑할 dto)
        // post 요청을 보내고 응답받은 json을 java 객체로 변환
        PaymentReadyDto.Response response = null;
        try {
            response = restTemplate.postForObject(
                READY_URL, requestEntity, PaymentReadyDto.Response.class
            );
        } catch (RestClientException e) {
            throw new PaymentReadyException("결제 준비 요청에 대한 응답이 없습니다.");
        }

        // order 테이블에 tid 저장
        order.setTid(response.getTid());
        orderRepository.save(order);

        return response;
    }

    // 결제 승인
    public PaymentApproveDto.Response approve(String partnerOrderId, String pgToken) {

        Member member = memberRepository.findById(1L)
            .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다.")); // TODO 토큰을 받아 처리하는 것으로 수정

        Long orderId = Long.valueOf(partnerOrderId);
        Orders order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("주문내역이 존재하지 않습니다."));

        // 요청으로 받은 값들을 String으로 변환
        String tidStr = String.valueOf(order.getTid());
        String memberIdStr = String.valueOf(order.getMember().getId());

        // 카카오로 보낼 결제 승인 요청에 필요한 정보들 생성
        // header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_AUTHORIZATION, "SECRET_KEY " + SECRET_KEY);
        headers.set(HEADER_CONTENT_TYPE, "application/json");

        // body 설정
        PaymentApproveDto.Request paymentRequest = PaymentApproveDto.Request.builder()
            .cid(CID)
            .tid(tidStr)
            .partnerOrderId(partnerOrderId)
            .partnerUserId(memberIdStr)
            .pgToken(pgToken)
            .build();

        // map으로 변환
        Map<String, String> map = paymentRequest.toMap();

        // header, body 하나로 합치기
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(map, headers);

        // 결제 승인 요청하기
        PaymentApproveDto.Response response = null;
        try {
            response = restTemplate.postForObject(
                APPROVE_URL, requestEntity, PaymentApproveDto.Response.class
            );
        } catch (RestClientException e) {
            throw new PaymentApproveException("결제 승인 요청에 대한 응답이 없습니다.");
        }

        // 회원 포인트 충전
        Integer point = response.getAmount().getTotal();
        member.addPoint(point);
        memberRepository.save(member);

        // 포인트 충전내역 저장
        PointHistory pointHistory = PointHistory.builder()
            .pointType(CHARGE)
            .pointAmount(point)
            .curPointAmount(member.getPoint())
            .member(member)
            .build();
        pointRepository.save(pointHistory);

        return response;
    }
}
