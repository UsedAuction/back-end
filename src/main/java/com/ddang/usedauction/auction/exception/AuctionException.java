package com.ddang.usedauction.auction.exception;

import lombok.Getter;

@Getter
public class AuctionException extends RuntimeException{

    private final AuctionErrorCode auctionErrorCode;

    public AuctionException(AuctionErrorCode auctionErrorCode){

        super(auctionErrorCode.getMessage());
        this.auctionErrorCode = auctionErrorCode;
    }
}
