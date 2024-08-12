package com.ddang.usedauction.order.dto;

import com.ddang.usedauction.order.domain.Orders;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class OrderCreateDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class Request {

        @Min(value = 1, message = "상품가격은 1이상이어야 합니다.")
        private int price; // 상품가격
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long orderId; // 주문 id
        private Long memberId; // 회원 id

        // dto로 변환
        public static Response from(Orders orders) {
            return Response.builder()
                .orderId(orders.getId())
                .memberId(orders.getMember().getId())
                .build();
        }
    }
}
