package com.ddang.usedauction.transaction.exception;

import lombok.Getter;

@Getter
public class TransactionException extends RuntimeException {

    private final TransactionErrorCode transactionErrorCode;

    public TransactionException(TransactionErrorCode transactionErrorCode) {

        super(transactionErrorCode.getMessage());
        this.transactionErrorCode = transactionErrorCode;
    }
}
