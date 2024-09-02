package com.ddang.usedauction.member.repository;

import com.ddang.usedauction.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberIdAndDeletedAtIsNull(String memberId); // 회원 아이디로 조회

    Optional<Member> findByEmailAndDeletedAtIsNull(String email); // 회원 이메일로 조회

    boolean existsByMemberIdAndDeletedAtIsNull(String memberId);

    boolean existsByEmailAndDeletedAtIsNull(String email);
}
