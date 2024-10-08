package com.ddang.usedauction.answer.repository;

import com.ddang.usedauction.answer.domain.Answer;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("select a from Answer a where a.auction.seller.memberId = :memberId")
    Optional<Answer> findByMemberId(String memberId); // 회원이 작성한 답변 조회

    // 회원이 작성한 답변 리스트 조회
    @Query("select a from Answer a where a.auction.seller.memberId = :memberId")
    Page<Answer> findAllByMemberId(String memberId, Pageable pageable);
}
