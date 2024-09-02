package com.ddang.usedauction.transaction.dto;

import com.ddang.usedauction.transaction.domain.Transaction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
public class TransactionDto {

    private long price;
    private Long sellerId;

    public static TransactionDto from(Transaction transaction) {

        return TransactionDto.builder()
            .price(transaction.getPrice())
            .sellerId(transaction.getAuction().getSeller().getId())
            .build();
    }
}
