package com.ddang.usedauction.auction.exception;

public class StartPriceOutOfBoundsException extends IndexOutOfBoundsException {

    public StartPriceOutOfBoundsException(long curStartPrice, long curInstantPrice) {
        super("입찰 시작가는 즉시 구매가보다 작아야 합니다. 현재 입찰 시작가: " + curStartPrice + ", 현재 즉시구매가: "
            + curInstantPrice);
    }
}
