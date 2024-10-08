package com.ddang.usedauction.bid.repository;

import com.ddang.usedauction.bid.domain.Bid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("select b from Bid b where b.member.memberId = :memberId")
    Page<Bid> findAllByMemberId(String memberId, Pageable pageable); // 회원 이메일로 입찰 조회

    @Query("select b from Bid b where b.member.id = :memberPk")
    List<Bid> findAllByMemberPk(Long memberPk);
}
