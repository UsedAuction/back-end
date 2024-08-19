package com.ddang.usedauction.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class PaymentInfoDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Request {

        @NotNull(message = "주문 id는 null 일 수 없습니다.")
        private Long orderId; // 주문 id

        @NotNull(message = "회원 id는 null 일 수 없습니다.")
        private Long memberId; // 회원 id

        @Min(value = 1, message = "상품가격은 1 이상이어야 합니다.")
        private int price; // 상품가격
    }
}
