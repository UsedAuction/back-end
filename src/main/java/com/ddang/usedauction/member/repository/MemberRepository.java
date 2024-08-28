package com.ddang.usedauction.member.repository;

import com.ddang.usedauction.member.domain.Member;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String memberId); // 회원 아이디로 조회
  
    Optional<Member> findByEmail(String email); // 회원 이메일로 조회

    boolean existsByMemberId(String memberId); // 중복 아이디 확인

    boolean existsByEmail(String email); // 중복 이메일 확인

}