package com.ddang.usedauction.bid.exception;

import lombok.Getter;

@Getter
public class MemberBidAmountOutOfBoundsException extends IndexOutOfBoundsException {

    public MemberBidAmountOutOfBoundsException(long memberPoint, long previousUsedPoint,
        long bidPoint) {

        super("현재 보유 포인트 : " + memberPoint + ", 다른 경매의 최고입찰가 총 합 포인트 : " + previousUsedPoint
            + ", 사용 가능 포인트 : " + (memberPoint - previousUsedPoint) + ", 현재 입찰할 금액 : " + bidPoint);
    }
}
