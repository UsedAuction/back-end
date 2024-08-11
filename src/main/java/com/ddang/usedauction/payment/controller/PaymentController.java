package com.ddang.usedauction.payment.controller;

import com.ddang.usedauction.payment.dto.PaymentApproveDto;
import com.ddang.usedauction.payment.dto.PaymentInfoDto;
import com.ddang.usedauction.payment.dto.PaymentReadyDto;
import com.ddang.usedauction.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/payment")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 준비
     * 1. 사용자가 카카오페이 버튼을 누른다.(/api/members/payment/ready)
     * 2. ready()에서 받은 요청을 통해 결제 준비에 필요한 정보로 가공하고 이를 카카오서버로 보낸다.
     * 3. 응답으로 결제 승인 요청에 필요한 정보(tid 등)를 받는다.
     * 4. 결제 승인 요청에 필요한 tid를 db에 저장한다. -> redis로 변경 예정
     *
     * @param request 주문 상세 정보
     * @return 성공 시 200 코드와 결제 승인에 필요한 정보(tid, redirect_url 등), 실패 시 에러코드와 에러메시지
     */
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyDto.Response> paymentReady(
        @RequestBody @Valid PaymentInfoDto.Request request
    ) {
        return ResponseEntity.ok(paymentService.ready(request));
    }

    /**
     * 결제 승인
     * 1. 카카오페이 포인트, 카드 등 결제 수단을 선택하고 결제하기 버튼을 누른다.
     * 2. faceID 등으로 결제 인증을 완료하면 pg_token과 tid로 결제 승인 요청을 한다.
     * 3. pg_token은 1번에서 결제 버튼을 눌러 approval_url로 리다이렉트 될 때 approval_url에 포함된 쿼리스트링으로 받는다.
     *    (http://localhost:8080/api/members/payment/approve?partner_order_id=1&pg_token=13123123123123123)
     * 4. tid는 이전에 결제 준비 단계에서 db에 저장된 값을 가져와 사용한다.
     * 5. tid를 가져오기 위해 approval_url에 쿼리스트링으로 partner_order_id을 넣었는데 이 값을 통해 가져올 수 있다.
     * 6. 승인이 완료되면 사용자의 포인트를 충전하고 포인트 충전내역을 생성해 저장한다.
     *
     * @param partnerOrderId 주문 id
     * @param pgToken 결제 준비 완료후 결제 인증(faceID 등 본인인증)을 통과하면 카카오쪽에서 pgToken을 응답으로 줌
     * @return 성공 시 200 코드와 결제 정보(아이템명, 수량 등), 실패 시 에러코드와 에러메시지
     */
    @PostMapping("/approve")
    public ResponseEntity<PaymentApproveDto.Response> paymentApprove(
        @RequestParam("partner_order_id") String partnerOrderId,
        @RequestParam("pg_token") String pgToken
    ) {
        return ResponseEntity.ok(paymentService.approve(partnerOrderId, pgToken));
    }
}
