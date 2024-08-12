package com.ddang.usedauction.payment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Amount {

    private Integer total; // 전체 결제 금액
    private Integer tax_free; // 비과세 금액

//    private Integer vat; // 부가세 금액
//    private Integer point; // 사용한 카카오페이 포인트 금액
//    private Integer discount; // 할인 금액
}
