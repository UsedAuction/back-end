package com.ddang.usedauction.transaction.repository;

import static com.ddang.usedauction.transaction.domain.QTransaction.transaction;

import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class TransactionRepositoryCustomImpl implements TransactionRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Transaction> findAllByTransactionListBySeller(String sellerEmail, String word,
        String transTypeString, String sorted, LocalDate startDate, LocalDate endDate,
        Pageable pageable) {

        List<Transaction> transactionList = jpaQueryFactory.selectFrom(transaction)
            .where(transaction.auction.seller.email.eq(sellerEmail), containsWord(word),
                eqTransType(transTypeString), betweenDate(startDate, endDate))
            .orderBy(getOrderSpecifier(sorted))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        int size = jpaQueryFactory.selectFrom(transaction)
            .where(transaction.auction.seller.memberId.eq(sellerEmail), containsWord(word),
                eqTransType(transTypeString))
            .fetch()
            .size();

        return new PageImpl<>(transactionList, pageable, size);
    }

    @Override
    public Page<Transaction> findAllByTransactionListByBuyer(String buyerEmail, String word,
        String transTypeString, String sorted, LocalDate startDate, LocalDate endDate,
        Pageable pageable) {

        List<Transaction> transactionList = jpaQueryFactory.selectFrom(transaction)
            .where(transaction.buyer.email.eq(buyerEmail), containsWord(word),
                eqTransType(transTypeString), betweenDate(startDate, endDate))
            .orderBy(getOrderSpecifier(sorted))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        int size = jpaQueryFactory.selectFrom(transaction)
            .where(transaction.buyer.memberId.eq(buyerEmail), containsWord(word),
                eqTransType(transTypeString))
            .fetch()
            .size();

        return new PageImpl<>(transactionList, pageable, size);
    }

    // 상품명이 검색어에 포함되는지 여부
    private BooleanExpression containsWord(String word) {

        if (!StringUtils.hasText(word)) {
            return null;
        }

        return transaction.auction.productName.contains(word);
    }

    // 거래 진행 중 또는 거래 종료 일치 여부
    private BooleanExpression eqTransType(String transTypeString) {

        final String CONTINUE = "continue";
        final String END = "end";

        if (transTypeString == null) {
            transTypeString = "";
        }

        if (transTypeString.equals(END)) {
            return transaction.transType.eq(TransType.NONE)
                .or(transaction.transType.eq(TransType.SUCCESS));
        }

        if (transTypeString.equals(CONTINUE)) {
            return transaction.transType.eq(TransType.CONTINUE);
        }

        return null;
    }

    private BooleanExpression betweenDate(LocalDate startDate, LocalDate endDate) {

        // startDate만 입력한 경우
        if (startDate != null && endDate == null) {
            return transaction.updatedAt.after(startDate.atStartOfDay());
        }

        // startDate가 null 인 경우
        if (startDate == null) {
            return null;
        }

        return transaction.updatedAt.between(startDate.atStartOfDay(), endDate.atTime(23, 59));
    }

    // 정렬 방법
    private OrderSpecifier<?> getOrderSpecifier(String sorted) {

        final String LOW = "low";
        final String HIGH = "high";
        final String OLD = "old";

        if (sorted == null) {
            sorted = "";
        }

        return switch (sorted) {
            case LOW -> transaction.price.asc(); // 판매 가격 낮은순
            case HIGH -> transaction.price.desc(); // 판매 가격 높은순
            case OLD -> transaction.updatedAt.asc(); // 오래된순
            default -> transaction.updatedAt.desc(); // 최신순
        };
    }
}
