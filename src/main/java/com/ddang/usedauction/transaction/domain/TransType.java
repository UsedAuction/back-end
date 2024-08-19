package com.ddang.usedauction.transaction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransType {

    SUCCESS("SUCCESS", "거래 완료"),
    NONE("NONE", "구매자 없이 경매 종료"),
    CONTINUE("CONTINUE", "거래 진행 중");

    private final String name;
    private final String description;
}
