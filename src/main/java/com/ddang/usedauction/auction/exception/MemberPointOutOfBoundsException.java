package com.ddang.usedauction.auction.exception;

public class MemberPointOutOfBoundsException extends IndexOutOfBoundsException {

    public MemberPointOutOfBoundsException(long curMemberPoint, long auctionPrice) {

        super("현재 포인트가 부족합니다. 현재 보유 포인트: " + curMemberPoint + ", 사용해야 하는 포인트: " + auctionPrice);
    }
}
