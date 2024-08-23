package com.ddang.usedauction.bid.repository;

import com.ddang.usedauction.bid.domain.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("select b from Bid b where b.member.email = :memberEmail")
    Page<Bid> findAllByMemberEmail(String memberEmail, Pageable pageable); // 회원 이메일로 입찰 조회
}
