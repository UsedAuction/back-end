package com.ddang.usedauction.global.security.auth;

import com.ddang.usedauction.global.security.auth.userInfo.GoogleUserInfo;
import com.ddang.usedauction.global.security.auth.userInfo.KakaoUserInfo;
import com.ddang.usedauction.global.security.auth.userInfo.NaverUserInfo;
import com.ddang.usedauction.global.security.auth.userInfo.Oauth2UserInfo;
import com.ddang.usedauction.member.domain.entity.Member;
import com.ddang.usedauction.member.domain.enums.Role;
import com.ddang.usedauction.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    Oauth2UserInfo oauth2UserInfo = null;
    String provider = userRequest.getClientRegistration().getRegistrationId();
    if (provider.equals("google")) {
      oauth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
    } else if (provider.equals("naver")) {
      oauth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
    } else if (provider.equals("kakao")) {
      oauth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
    }

    String providerId = oauth2UserInfo.getProviderId();
    String email = oauth2UserInfo.getEmail();
    String memberId = oauth2UserInfo.getProvider() + "_" + providerId;
    String passWord = passwordEncoder.encode("passWord");

    Member member = memberRepository.findByEmail(email)
        .orElseGet(() -> signUp(memberId, passWord, email, provider, providerId));

    return new PrincipalDetails(member.getId(), member.getMemberId(), member.getPassWord(),
        member.getEmail(), member.getRole().toString(), oauth2UserInfo);
  }

  private Member signUp(String memberId, String passWord, String email, String provider,
      String providerId) {
    Member user = Member.builder()
        .memberId(memberId)
        .passWord(passWord)
        .email(email)
        .social(provider)
        .socialProviderId(providerId)
        .role(Role.USER)
        .build();

    return memberRepository.save(user);
  }
}
