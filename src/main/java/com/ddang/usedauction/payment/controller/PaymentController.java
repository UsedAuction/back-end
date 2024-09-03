package com.ddang.usedauction.payment.controller;

import com.ddang.usedauction.payment.dto.PaymentApproveDto;
import com.ddang.usedauction.payment.dto.PaymentCancelDto;
import com.ddang.usedauction.payment.dto.PaymentFailDto;
import com.ddang.usedauction.payment.dto.PaymentInfoDto;
import com.ddang.usedauction.payment.dto.PaymentReadyDto;
import com.ddang.usedauction.payment.service.PaymentService;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/payment")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 준비
     *
     * @param request 주문 상세 정보
     * @return 성공 시 200 코드와 결제 승인에 필요한 정보, 실패 시 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/ready")
    public ResponseEntity<PaymentReadyDto.Response> paymentReady(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @RequestBody @Valid PaymentInfoDto.Request request
    ) {
        String memberId = principalDetails.getName();
        return ResponseEntity.ok(paymentService.ready(memberId, request));
    }

    /**
     * 결제 승인
     *
     * @param partnerOrderId 주문 id
     * @param pgToken        카카오쪽에서 받은 pgToken
     * @return 성공 시 200 코드와 결제 정보, 실패 시 에러메시지
     */
    @GetMapping("/approve")
    public ResponseEntity<PaymentApproveDto.Response> paymentApprove(
        @RequestParam("partner_order_id") String partnerOrderId,
        @RequestParam("pg_token") String pgToken
    ) {
        return ResponseEntity.ok(paymentService.approve(partnerOrderId, pgToken));
    }

    /**
     * 결제 취소
     */
    @GetMapping("/cancel")
    public ResponseEntity<PaymentCancelDto.Response> paymentCancel() {
        return ResponseEntity.ok(new PaymentCancelDto.Response("결제를 취소했습니다."));
    }

    /**
     * 결제 실패
     */
    @GetMapping("/fail")
    public ResponseEntity<PaymentFailDto.Response> paymentFail() {
        return ResponseEntity.ok(new PaymentFailDto.Response("결제가 실패되었습니다."));
    }
}
