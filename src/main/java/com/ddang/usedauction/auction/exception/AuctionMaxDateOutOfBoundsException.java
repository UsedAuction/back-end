package com.ddang.usedauction.auction.exception;

public class AuctionMaxDateOutOfBoundsException extends IndexOutOfBoundsException {

    public AuctionMaxDateOutOfBoundsException() {
        super("경매를 진행할 수 있는 최대 기간은 일주일입니다.");
    }
}
