package com.ddang.usedauction.auction.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode {

    LOW_PRICE(HttpStatus.BAD_REQUEST.value(), "즉시구매가는 입찰시작가보다 큰 수이어야 합니다."),
    END_DATE_IS_AFTER_7(HttpStatus.BAD_REQUEST.value(), "경매는 최대 일주일동안 진행할 수 있습니다."),
    TOO_MANY_IMAGE(HttpStatus.BAD_REQUEST.value(), "이미지는 6개까지 등록 가능합니다."),
    NOT_FOUND_AUCTION(HttpStatus.BAD_REQUEST.value(), "등록되지 않은 경매글입니다.");

    private final int status;
    private final String message;
}
