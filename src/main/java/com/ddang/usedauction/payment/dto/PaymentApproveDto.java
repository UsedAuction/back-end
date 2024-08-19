package com.ddang.usedauction.payment.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class PaymentApproveDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Request {

        private String cid; // 가맹점코드 (테스트용이라 "TC0ONETIME"로 고정)
        private String tid; // 결제 고유번호, 결제 준비 API 응답에 포함
        private String partnerOrderId; // 주문 id
        private String partnerUserId; // 회원 id
        private String pgToken; // 결제승인 요청을 인증하는 토큰. 사용자 결제 수단 선택 완료 시, approval_url로 redirection 해줄 때 pg_token을 query string으로 전달

        // map으로 변환
        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            map.put("cid", this.cid);
            map.put("tid", this.tid);
            map.put("partner_order_id", this.partnerOrderId);
            map.put("partner_user_id", this.partnerUserId);
            map.put("pg_token", this.pgToken);
            return map;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Response {

        private String aid; // 요청 고유 번호 - 승인/취소가 구분된 결제번호
        private String tid; // 결제 고유 번호 - 승인/취소가 동일한 결제번호
        private String cid; // 가맹점 코드
        private String partner_order_id; // 주문 id
        private String partner_user_id; // 회원 id
        private String payment_method_type; // 결제 수단, CARD 또는 MONEY 중 하나
        private Amount amount; // 결제 금액 정보
        private String item_name; // 상품명
        private Integer quantity; // 상품 수량
        private LocalDateTime created_at; // 결제 준비 요청 시각
        private LocalDateTime approved_at; // 결제 승인 시각
    }
}
