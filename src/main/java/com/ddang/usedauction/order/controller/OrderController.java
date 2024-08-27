package com.ddang.usedauction.order.controller;

import com.ddang.usedauction.order.domain.Orders;
import com.ddang.usedauction.order.dto.OrderCreateDto;
import com.ddang.usedauction.order.service.OrderService;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 1. 포인트 충전 모달에서 구매할 포인트를 선택한다. 2. 다음 버튼을 누른다. -> 주문생성
     *
     * @param principalDetails 회원 정보
     * @param request          주문 요청 정보
     * @return 성공 시 201 코드와 주문 pk 및 회원 pk,  실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create")
    public ResponseEntity<OrderCreateDto.Response> createOrder(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @RequestBody @Valid OrderCreateDto.Request request
    ) {
        String email = principalDetails.getName();
        Orders order = orderService.createOrder(email, request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(OrderCreateDto.Response.from(order));
    }
}
