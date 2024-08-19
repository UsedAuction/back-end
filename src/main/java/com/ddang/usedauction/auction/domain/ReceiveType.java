package com.ddang.usedauction.auction.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReceiveType {

    CONTACT("CONTACT", "대면 거래"),
    DELIVERY("DELIVERY", "택배"),
    ALL("ALL", "모두 가능");

    private final String name;
    private final String description;

    // Enum 검증을 위한 코드, Enum에 속하지 않으면 null 리턴
    @JsonCreator
    private static ReceiveType fromTransactionType(String value) {

        return Arrays.stream(ReceiveType.values())
            .filter(r -> r.getName().equals(value.toUpperCase()))
            .findAny()
            .orElse(null);
    }
}
