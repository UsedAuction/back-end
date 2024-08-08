package com.ddang.usedauction.payment.dto;

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
        private String cid;
        private String partnerOrderId;
        private String partnerUserId;
        private String itemName;
        private String quantity;
        private String totalAmount;
        private String taxFreeAmount;
        private String approvalUrl;
        private String cancelUrl;
        private String failUrl;

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
            map.put("approval_url", "http://localhost:8080/payment/success?partner_order_id=" + this.partnerOrderId);
            map.put("cancel_url", "http://localhost:8080/payment/cancel");
            map.put("fail_url", "http://localhost:8080/payment/fail");
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
        private String created_at; // 결제준비를 요청한 시간
    }
}
