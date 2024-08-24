package com.ddang.usedauction.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BidStatus {

    SUCCESS("입찰 성공"),
    BID_FAIL("먼저 입찰한 회원이 있는 경우"),
    POINT_FAIL("포인트가 부족한 경우"),
    ERROR("에러 발생");

    private final String description;
}
