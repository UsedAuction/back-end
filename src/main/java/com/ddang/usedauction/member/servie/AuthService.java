package com.ddang.usedauction.member.servie;

import com.ddang.usedauction.mail.service.MailPasswordService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.domain.enums.Role;
import com.ddang.usedauction.member.dto.MemberChangeEmailDto;
import com.ddang.usedauction.member.dto.MemberChangePasswordDto;
import com.ddang.usedauction.member.dto.MemberCheckIdDto;
import com.ddang.usedauction.member.dto.MemberFindIdDto;
import com.ddang.usedauction.member.dto.MemberFindPasswordDto;
import com.ddang.usedauction.member.dto.MemberLoginRequestDto;
import com.ddang.usedauction.member.dto.MemberLoginResponseDto;
import com.ddang.usedauction.member.dto.MemberSignUpDto;
import com.ddang.usedauction.member.exception.CustomMemberException;
import com.ddang.usedauction.member.exception.MemberErrorCode;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.security.jwt.exception.CustomJwtException;
import com.ddang.usedauction.security.jwt.exception.JwtErrorCode;
import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.ddang.usedauction.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailPasswordService mailPasswordService;

    public MemberLoginResponseDto login(HttpServletResponse response,
        @RequestBody MemberLoginRequestDto dto) {
        Member member = memberRepository.findByMemberId(dto.getMemberId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassWord())) {
            throw new CustomMemberException(MemberErrorCode.NOT_MATCHED_PASSWORD);
        }

        GrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().toString());
        List<GrantedAuthority> authorities = Collections.singletonList(authority);

        TokenDto token = tokenProvider.generateToken(member.getMemberId(), authorities);

        long refreshTokenExpiration = tokenProvider.getExpiration(token.getRefreshToken());
        refreshTokenService.save(token.getAccessToken(), token.getRefreshToken(),
            refreshTokenExpiration);

        CookieUtil.addCookie(response, "refreshToken", token.getRefreshToken(),
            (int) refreshTokenExpiration);

        response.setHeader("New-Token", token.getAccessToken());

        return MemberLoginResponseDto.builder()
            .accessToken(token.getAccessToken())
            .memberId(member.getMemberId())
            .build();
    }

    public void checkMemberId(MemberCheckIdDto dto) {
        if (memberRepository.existsByMemberId(dto.getMemberId())) {
            throw new CustomMemberException(MemberErrorCode.ALREADY_EXISTS_MEMBER_ID);
        }
    }

    @Transactional
    public void signUp(MemberSignUpDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new CustomMemberException(MemberErrorCode.NOT_MATCHED_PASSWORD);
        }

        if (memberRepository.existsByMemberId(dto.getMemberId())) {
            throw new CustomMemberException(MemberErrorCode.ALREADY_EXISTS_MEMBER_ID);
        }

        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new CustomMemberException(MemberErrorCode.ALREADY_EXISTS_EMAIL);
        }

        MemberSignUpDto.from(
            memberRepository.save(Member.builder()
                .memberId(dto.getMemberId())
                .passWord(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .role(Role.ROLE_USER)
                .build())
        );
    }

    public void changeEmail(String memberId, MemberChangeEmailDto dto) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new CustomMemberException(MemberErrorCode.DUPLICATED_EMAIL);
        }

        member.updateEmail(dto.getEmail());

        memberRepository.save(member);

    }

    public void changePassword(String memberId, MemberChangePasswordDto dto) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(dto.getCurPassword(), member.getPassWord())) {
            throw new CustomMemberException(MemberErrorCode.NOT_MATCHED_PASSWORD);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new CustomMemberException(MemberErrorCode.NOT_MATCHED_CHECK_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(dto.getNewPassword()));

        memberRepository.save(member);
    }

    public String findMemberId(MemberFindIdDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new NoSuchElementException("가입된 아이디가 존재하지 않습니다."));

        return member.getMemberId();
    }

    public void findPassword(MemberFindPasswordDto dto) {
        if (!memberRepository.existsByMemberId(dto.getMemberId())) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }
        Member member = memberRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        String newPassword = UUID.randomUUID().toString().substring(0, 8);

        member.updatePassword(passwordEncoder.encode(newPassword));
        mailPasswordService.sendMessage(member.getEmail(), newPassword);

        memberRepository.save(member);
    }

    @Transactional
    public void logout(String memberId, HttpServletRequest request, HttpServletResponse response) {
        String accessToken = tokenProvider.resolveTokenFromRequest(request);

        if (!memberId.equals(tokenProvider.getMemberIdByToken(accessToken))) {
            throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
        }

        long accessTokenExpiration = tokenProvider.getExpiration(accessToken);

        CookieUtil.deleteCookie(request, response, "refreshToken");

        refreshTokenService.deleteRefreshTokenByAccessToken(accessToken);
        refreshTokenService.setBlackList(accessToken, "accessToken",
            accessTokenExpiration);

        // 보안 컨텍스트에서 인증 정보 제거
        SecurityContextHolder.clearContext();

    }

    public void withdrawal(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        member.withdrawal();

        memberRepository.save(member);
    }
}
