package com.ddang.usedauction.ask.repository;

import com.ddang.usedauction.ask.domain.Ask;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AskRepository extends JpaRepository<Ask, Long> {

    // 회원이 작성한 문의 조회
    @Query("select a from Ask a where a.writer.email = :memberId")
    Optional<Ask> findByMemberEmail(String memberEmail);

    // 회원이 작성한 문의 리스트 조회
    @Query("select a from Ask a where a.writer.email = :memberEmail")
    Page<Ask> findAllByMemberEmail(String memberEmail, Pageable pageable);
}
