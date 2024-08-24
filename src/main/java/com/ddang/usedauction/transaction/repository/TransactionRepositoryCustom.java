package com.ddang.usedauction.transaction.repository;

import com.ddang.usedauction.transaction.domain.Transaction;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionRepositoryCustom {

    // 판매 내역 조회
    Page<Transaction> findAllByTransactionListBySeller(String sellerEmail, String word,
        String transTypeString,
        String sorted, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // 구매 내역 조회
    Page<Transaction> findAllByTransactionListByBuyer(String buyerEmail, String word,
        String transTypeString,
        String sorted, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
