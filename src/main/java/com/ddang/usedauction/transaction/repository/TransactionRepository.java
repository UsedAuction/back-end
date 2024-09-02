package com.ddang.usedauction.transaction.repository;

import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
    TransactionRepositoryCustom {

    @Query("select t from Transaction t where t.buyer.memberId = :buyerId and t.auction.id = :auctionId")
    Optional<Transaction> findByBuyerIdAndAuctionId(String buyerId,
        Long auctionId); // 구매자 아이디와 경매 pk로 거래 내역 조회

    @Query("select t from Transaction t where t.auction.id = :auctionId")
    Optional<Transaction> findByAuctionId(Long auctionId); // 경매 pk 로 거래 조회

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END from Transaction t where t.auction.seller.memberId = :memberId or t.buyer.memberId = :memberId and t.transType = :transType")
    boolean existsByUser(String memberId, TransType transType);
}
