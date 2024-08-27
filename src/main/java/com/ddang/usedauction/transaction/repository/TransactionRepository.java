package com.ddang.usedauction.transaction.repository;

import com.ddang.usedauction.transaction.domain.Transaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
    TransactionRepositoryCustom {

    @Query("select t from Transaction t where t.buyer.email = :buyerEmail and t.auction.id = :auctionId")
    Optional<Transaction> findByBuyerEmailAndAuctionId(String buyerEmail,
        Long auctionId); // 구매자 아이디와 경매 pk로 거래 내역 조회
}
