package com.ddang.usedauction.auction.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode {

    NOT_FOUND_AUCTION(HttpStatus.BAD_REQUEST.value(), "등록되지 않은 경매글입니다.");

    private final int status;
    private final String message;
}
