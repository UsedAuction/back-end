package com.ddang.usedauction.auction.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionState {

    CONTINUE("continue", "경매 진행 중"),
    END("end", "경매 종료");

    private final String name;
    private final String description;

    // Enum 검증을 위한 코드, Enum에 속하지 않으면 null 리턴
    @JsonCreator
    private static AuctionState fromAuctionState(String value) {

        return Arrays.stream(AuctionState.values())
            .filter(r -> r.getName().equals(value.toUpperCase()))
            .findAny()
            .orElse(null);
    }
}
