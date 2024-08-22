package com.ddang.usedauction.auction.repository;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionRepositoryCustom {

    @Query("select a from Auction a inner join a.bidList b where b.member.memberId = :memberId and a.auctionState = :auctionState")
    List<Auction> findAllByMemberIdAndAuctionState(String memberId,
        AuctionState auctionState); // 현재 회원이 참여중인 경매 리스트 조회
}
