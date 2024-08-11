package com.ddang.usedauction.global.security.auth;

import com.ddang.usedauction.member.domain.entity.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Member member = memberRepository.findByEmail(username)
        .orElseThrow(() -> new BadCredentialsException("등록되지 않은 회원입니다."));

    return new PrincipalDetails(member.getId(), member.getMemberId(), member.getPassWord(),
        member.getEmail(), member.getRole().toString());
  }
}
