package com.ddang.usedauction.member.repository;

import com.ddang.usedauction.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String memberId); // 회원 아이디로 조회
}
