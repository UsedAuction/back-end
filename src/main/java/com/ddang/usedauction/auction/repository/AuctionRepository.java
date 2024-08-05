package com.ddang.usedauction.auction.repository;

import com.ddang.usedauction.auction.domain.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

}
