package com.ddang.usedauction.auction.repository;

import com.ddang.usedauction.auction.domain.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionRepositoryCustom {

    Page<Auction> findAllByOptions(String word, String category, String sorted, Pageable pageable);
}
