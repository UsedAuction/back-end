package com.ddang.usedauction.auction.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryType {

    PREPAY("prepay", "선불"),
    NO_PREPAY("noprepay", "착불"),
    NO_DELIVERY("nodelivery", "택배 안함");

    private final String name;
    private final String description;

    // Enum 검증을 위한 코드, Enum에 속하지 않으면 null 리턴
    @JsonCreator
    private static DeliveryType fromDeliveryType(String value) {

        return Arrays.stream(DeliveryType.values())
            .filter(r -> r.getName().equals(value.toUpperCase()))
            .findAny()
            .orElse(null);
    }
}
