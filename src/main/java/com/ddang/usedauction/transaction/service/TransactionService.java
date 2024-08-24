package com.ddang.usedauction.transaction.service;

import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * 판매 내역 조회 서비스
     *
     * @param sellerEmail     판매자 이메일
     * @param word            검색어
     * @param transTypeString 거래 진행 중 또는 거래 종료
     * @param sorted          정렬
     * @param startDate       시작 날짜
     * @param endDate         끝 날짜
     * @param pageable        페이징
     * @return 페이징된 거래 내역 리스트
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionListBySeller(String sellerEmail, String word,
        String transTypeString, String sorted, LocalDate startDate, LocalDate endDate,
        Pageable pageable) {

        return transactionRepository.findAllByTransactionListBySeller(
            sellerEmail, word, transTypeString, sorted, startDate, endDate, pageable);
    }

    /**
     * 구매 내역 조회 서비스
     *
     * @param buyerEmail      구매자 이메일
     * @param word            검색어
     * @param transTypeString 거래 종료 또는 거래 진행 중
     * @param sorted          정렬
     * @param startDate       시작 날짜
     * @param endDate         끝 날짜
     * @param pageable        페이징
     * @return 페이징 처리된 거래 내역 리스트
     */
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionListByBuyer(String buyerEmail, String word,
        String transTypeString, String sorted, LocalDate startDate, LocalDate endDate,
        Pageable pageable) {

        return transactionRepository.findAllByTransactionListByBuyer(
            buyerEmail, word, transTypeString, sorted, startDate, endDate, pageable);
    }
}
