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

public class PaymentReadyDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Request {
        private String cid; // 가맹점코드 (테스트용이라 "TC0ONETIME"로 고정)
        private String partnerOrderId; // 주문id
        private String partnerUserId; // 유저id
        private String itemName; // 상품명(100포인트, 200포인트)
        private String quantity; // 상품 수량
        private String totalAmount; // 상품 가격
        private String taxFreeAmount; // 상품 비과세 금액 (0으로 고정)
        private String approvalUrl; // 결제 성공 시 redirect url
        private String cancelUrl; // 결제 취소 시 redirect url
        private String failUrl; // 결제 실패 시 redirect url

        //  map으로 변환
        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            map.put("cid", this.cid);
            map.put("partner_order_id", this.partnerOrderId);
            map.put("partner_user_id", this.partnerUserId);
            map.put("item_name", this.itemName);
            map.put("quantity", "1");
            map.put("total_amount", this.totalAmount);
            map.put("tax_free_amount", "0");
            map.put("approval_url", "https://dddang.store/api/members/payment/approve?partner_order_id=" + this.partnerOrderId);
            map.put("cancel_url", "https://dddang.store/api/members/payment/cancel");
            map.put("fail_url", "https://dddang.store/api/members/payment/fail");
            return map;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @ToString
    public static class Response {

        private String tid; // 결제 고유 번호
        private String next_redirect_pc_url; // 요청한 클라이언트가 PC 웹일 경우 카카오톡으로 결제 요청 메시지(TMS)를 보내기 위한 사용자 정보 입력 화면 Redirect URL
        private LocalDateTime created_at; // 결제준비를 요청한 시간
    }
}
