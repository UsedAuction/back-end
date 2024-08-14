package com.ddang.usedauction.transaction.controller;

import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.dto.TransactionGetDto;
import com.ddang.usedauction.transaction.service.TransactionService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 판매 내역 조회 컨트롤러
     *
     * @param word            검색어
     * @param transTypeString 거래 종료 또는 거래 진행 중
     * @param sorted          정렬
     * @param startDate       시작 날짜
     * @param endDate         끝 날짜
     * @param pageable        페이징
     * @return 성공 시 200 코드와 판매 내역 리스트, 실패 시 에러코드와 에러메시지
     */
    @GetMapping("/sales")
    public ResponseEntity<Page<TransactionGetDto.Response>> getTransactionListBySellerController(
        @RequestParam(required = false) String word,
        @RequestParam(required = false) String transTypeString,
        @RequestParam(required = false) String sorted,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate, @PageableDefault
    Pageable pageable) {

        String memberId = "test"; // todo: 토큰을 사용한 조회

        Page<Transaction> transactionPageList = transactionService.getTransactionListBySeller(
            memberId, word, transTypeString, sorted, startDate, endDate, pageable);

        return ResponseEntity.ok(transactionPageList.map(TransactionGetDto.Response::from));
    }

    /**
     * 구매 내역 조회
     *
     * @param word            검색어
     * @param transTypeString 거래 상태
     * @param sorted          정렬
     * @param startDate       시작 날짜
     * @param endDate         끝 날짜
     * @param pageable        페이징
     * @return 성공 시 200 코드와 구매 내역 리스트, 실패 시 에러코드와 에러메시지
     */
    @GetMapping("/purchases")
    public ResponseEntity<Page<TransactionGetDto.Response>> getTransactionListByBuyerController(
        @RequestParam(required = false) String word,
        @RequestParam(required = false) String transTypeString,
        @RequestParam(required = false) String sorted,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate, @PageableDefault
    Pageable pageable) {

        String memberId = "test"; // todo: 토큰을 사용한 조회

        Page<Transaction> transactionPageList = transactionService.getTransactionListByBuyer(
            memberId, word, transTypeString, sorted, startDate, endDate, pageable);

        return ResponseEntity.ok(transactionPageList.map(TransactionGetDto.Response::from));
    }
}
