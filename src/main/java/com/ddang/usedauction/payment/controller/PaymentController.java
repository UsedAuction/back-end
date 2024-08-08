package com.ddang.usedauction.payment.controller;

import com.ddang.usedauction.config.GlobalApiResponse;
import com.ddang.usedauction.payment.dto.PaymentInfoDto;
import com.ddang.usedauction.payment.dto.PaymentReadyDto;
import com.ddang.usedauction.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 2. ready()에서 받은 요청을 통해 결제 준비에 필요한 정보로 가공하고 이를 카카오서버로 요청한다.
     *
     * @param request 주문 상세 정보
     * @return 성공 시 200 코드와 결제 승인에 필요한 정보(tid, redirect_url 등), 실패 시 에러코드와 에러메시지
     */
    @PostMapping("/ready")
    public ResponseEntity<?> paymentReady(
//        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody @Valid PaymentInfoDto.Request request
    ) {
        PaymentReadyDto.Response response = paymentService.ready(request); // (userDetails, request);
        return ResponseEntity.ok(GlobalApiResponse.toGlobalResponse(HttpStatus.OK, response));
    }
}
