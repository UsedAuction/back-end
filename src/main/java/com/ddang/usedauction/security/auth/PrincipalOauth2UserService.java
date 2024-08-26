package com.ddang.usedauction.security.auth;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.domain.enums.Role;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.security.auth.userInfo.GoogleUserInfo;
import com.ddang.usedauction.security.auth.userInfo.KakaoUserInfo;
import com.ddang.usedauction.security.auth.userInfo.NaverUserInfo;
import com.ddang.usedauction.security.auth.userInfo.Oauth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * OAuth2 인증 처리
 */
@Slf4j
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
        String email = oauth2UserInfo.getEmail() + provider;
        String memberId = oauth2UserInfo.getProvider() + "_" + providerId;
        String passWord = passwordEncoder.encode("passWord");

        // 플랫폼별 로그인 시 이메일이 중복되어도 개별적으로 회원가입 가능
        Member member = memberRepository.findByEmail(email)
            .orElseGet(() -> signUp(memberId, passWord, email, provider, providerId));

        return new PrincipalDetails(member.getEmail(), member.getPassWord(),
            member.getRole().toString(), oauth2UserInfo);
    }

    private Member signUp(String memberId, String passWord, String email, String provider,
        String providerId) {

        Member user = Member.builder()
            .memberId(memberId)
            .passWord(passWord)
            .email(email)
            .social(provider)
            .socialProviderId(providerId)
            .siteAlarm(true)
            .role(Role.ROLE_USER)
            .build();

        return memberRepository.save(user);
    }
}
