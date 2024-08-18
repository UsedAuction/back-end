package com.ddang.usedauction.security.auth;

import com.ddang.usedauction.member.domain.entity.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 일반 로그인 인증 처리
 */
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Member member = memberRepository.findByEmail(username)
        .orElseThrow(() -> new NoSuchElementException("등록되지 않은 회원입니다."));

    return new PrincipalDetails(member.getId(), member.getMemberId(), member.getPassWord(),
        member.getEmail(), member.getRole().toString());
  }
}
