package com.ddang.usedauction.point.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointType {

    USE("USE", "사용"),
    GET("GET", "판매 수익"),
    CHARGE("CHARGE", "충전");

    private final String name;
    private final String description;
}
