package com.ddang.usedauction.transaction.repository;

import com.ddang.usedauction.transaction.domain.Transaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
    TransactionRepositoryCustom {

    @Query("select t from Transaction t where t.buyer.id = :buyerId and t.auction.id = :auctionId")
    Optional<Transaction> findByBuyerId(Long buyerId, Long auctionId); // 구매자 PK와 경매 PK로 거래 내역 조회
}
