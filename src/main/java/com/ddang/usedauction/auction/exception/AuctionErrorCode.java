package com.ddang.usedauction.auction.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode {

    ALREADY_END_AUCTION(HttpStatus.BAD_REQUEST.value(), "이미 종료된 경매입니다."),
    FAIL_CONFIRM_AUCTION_BY_BUYER(HttpStatus.BAD_REQUEST.value(), "구매 확정 실패 -> 회원 포인트가 부족합니다."),
    CONTINUE_AUCTION(HttpStatus.BAD_REQUEST.value(), "아직 경매가 진행중입니다."),
    LOW_PRICE(HttpStatus.BAD_REQUEST.value(), "즉시구매가는 입찰시작가보다 큰 수이어야 합니다."),
    END_DATE_IS_BEFORE_NOW(HttpStatus.BAD_REQUEST.value(), "경매 종료 날짜는 현재 날짜보다 이전일 수 없습니다."),
    END_DATE_IS_AFTER_7(HttpStatus.BAD_REQUEST.value(), "경매는 최대 일주일동안 진행할 수 있습니다."),
    TOO_MANY_IMAGE(HttpStatus.BAD_REQUEST.value(), "이미지는 6개까지 등록 가능합니다."),
    NOT_FOUND_AUCTION(HttpStatus.BAD_REQUEST.value(), "등록되지 않은 경매글입니다.");

    private final int status;
    private final String message;
}
