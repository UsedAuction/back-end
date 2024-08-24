package com.ddang.usedauction.transaction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BuyType {

    SUCCESSFUL_BID("SUCCESSFULBID", "낙찰"),
    NO_BUY("NOBUY", "구매자 없음"),
    INSTANT("INSTANT", "즉시 구매");

    private final String name;
    private final String description;
}
