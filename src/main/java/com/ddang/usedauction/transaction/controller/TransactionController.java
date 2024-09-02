package com.ddang.usedauction.transaction.controller;

import com.ddang.usedauction.security.auth.PrincipalDetails;
import com.ddang.usedauction.transaction.dto.TransactionDto;
import com.ddang.usedauction.transaction.dto.TransactionGetDto;
import com.ddang.usedauction.transaction.dto.TransactionGetDto.Response;
import com.ddang.usedauction.transaction.service.TransactionService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * 경매 pk로 거래 조회
     *
     * @param auctionId 경매 pk
     * @return 성공 시 200 코드와 거래 내역, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<TransactionDto> getTransaction(
        @NotNull(message = "pk 값은 null 일 수 없습니다.") @Positive(message = "pk 값은 0 또는 음수일 수 없습니다.") @RequestParam Long auctionId) {

        TransactionDto transaction = transactionService.getTransaction(auctionId);

        return ResponseEntity.ok(transaction);
    }

    /**
     * 판매 내역 조회 컨트롤러
     *
     * @param word             검색어
     * @param transTypeString  거래 종료 또는 거래 진행 중
     * @param sorted           정렬
     * @param startDate        시작 날짜
     * @param endDate          끝 날짜
     * @param pageable         페이징
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드와 판매 내역 리스트, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/sales")
    public ResponseEntity<Page<TransactionGetDto.Response>> getTransactionListBySellerController(
        @RequestParam(required = false) String word,
        @RequestParam(required = false) String transTypeString,
        @RequestParam(required = false) String sorted,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate, @PageableDefault
    Pageable pageable, @AuthenticationPrincipal PrincipalDetails principalDetails) {

        String memberId = principalDetails.getName();

        Page<Response> transactionPageList = transactionService.getTransactionListBySeller(
            memberId, word, transTypeString, sorted, startDate, endDate, pageable);

        return ResponseEntity.ok(transactionPageList);
    }

    /**
     * 구매 내역 조회
     *
     * @param word             검색어
     * @param transTypeString  거래 상태
     * @param sorted           정렬
     * @param startDate        시작 날짜
     * @param endDate          끝 날짜
     * @param pageable         페이징
     * @param principalDetails 회원 정보
     * @return 성공 시 200 코드와 구매 내역 리스트, 실패 시 에러코드와 에러메시지
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/purchases")
    public ResponseEntity<Page<TransactionGetDto.Response>> getTransactionListByBuyerController(
        @RequestParam(required = false) String word,
        @RequestParam(required = false) String transTypeString,
        @RequestParam(required = false) String sorted,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate, @PageableDefault
    Pageable pageable, @AuthenticationPrincipal PrincipalDetails principalDetails) {

        String memberId = principalDetails.getName();

        Page<Response> transactionPageList = transactionService.getTransactionListByBuyer(
            memberId, word, transTypeString, sorted, startDate, endDate, pageable);

        return ResponseEntity.ok(transactionPageList);
    }
}
