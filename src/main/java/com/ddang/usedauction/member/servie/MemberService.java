package com.ddang.usedauction.member.servie;

import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.bid.repository.BidRepository;
import com.ddang.usedauction.mail.service.MailPasswordService;
import com.ddang.usedauction.mail.service.MailRedisService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.domain.enums.Role;
import com.ddang.usedauction.member.dto.MemberChangeEmailDto;
import com.ddang.usedauction.member.dto.MemberChangePasswordDto;
import com.ddang.usedauction.member.dto.MemberCheckIdDto;
import com.ddang.usedauction.member.dto.MemberFindIdDto;
import com.ddang.usedauction.member.dto.MemberFindPasswordDto;
import com.ddang.usedauction.member.dto.MemberGetDto;
import com.ddang.usedauction.member.dto.MemberLoginRequestDto;
import com.ddang.usedauction.member.dto.MemberLoginResponseDto;
import com.ddang.usedauction.member.dto.MemberSignUpDto;
import com.ddang.usedauction.member.exception.MemberErrorCode;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.repository.EmitterRepository;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import com.ddang.usedauction.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuctionRepository auctionRepository;
    private final TransactionRepository transactionRepository;
    private final BidRepository bidRepository;
    private final MailPasswordService mailPasswordService;
    private final MailRedisService mailRedisService;
    private final EmitterRepository emitterRepository;

    /**
     * 회원 정보 조회
     *
     * @param memberId 조회할 회원 아이디
     * @return 조회된 회원 정보
     */
    @Transactional(readOnly = true)
    public MemberGetDto.Response getMember(String memberId) {

        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return MemberGetDto.Response.from(member);
    }

    public MemberLoginResponseDto login(HttpServletResponse response,
        @RequestBody MemberLoginRequestDto dto) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(dto.getMemberId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassWord())) {
            throw new MemberException(MemberErrorCode.NOT_MATCHED_PASSWORD);
        }

        GrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().toString());
        List<GrantedAuthority> authorities = Collections.singletonList(authority);

        TokenDto token = tokenProvider.generateToken(member.getMemberId(), authorities);

        long refreshTokenExpiration = tokenProvider.getExpiration(token.getRefreshToken());
        refreshTokenService.save(token.getAccessToken(), token.getRefreshToken(),
            refreshTokenExpiration);

        CookieUtil.addCookie(response, "refreshToken", token.getRefreshToken());

        return MemberLoginResponseDto.builder()
            .accessToken(token.getAccessToken())
            .memberId(member.getMemberId())
            .build();
    }

    public void checkMemberId(MemberCheckIdDto dto) {
        log.info("id = {}", dto.getMemberId());
        if (memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())) {
            throw new MemberException(MemberErrorCode.ALREADY_EXISTS_MEMBER_ID);
        }
    }

    @Transactional
    public void signUp(MemberSignUpDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new MemberException(MemberErrorCode.NOT_MATCHED_PASSWORD);
        }

        if (memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())) {
            throw new MemberException(MemberErrorCode.ALREADY_EXISTS_MEMBER_ID);
        }

        if (memberRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())) {
            throw new MemberException(MemberErrorCode.ALREADY_EXISTS_EMAIL);
        }

        if (!dto.getEmail().equals(mailRedisService.getData(dto.getAuthNum()))) {
            throw new MemberException(MemberErrorCode.NOT_MATCHED_AUTHNUM);
        }

        MemberSignUpDto.from(
            memberRepository.save(Member.builder()
                .memberId(dto.getMemberId())
                .passWord(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .role(Role.ROLE_USER)
                .build())
        );

        mailRedisService.deleteData(dto.getAuthNum());
    }

    public void changeEmail(String memberId, MemberChangeEmailDto dto) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        if (member.getEmail().equals(dto.getEmail())) {
            throw new MemberException(MemberErrorCode.DUPLICATED_EMAIL);
        }

        if (!dto.getEmail().equals(mailRedisService.getData(dto.getAuthNum()))) {
            throw new MemberException(MemberErrorCode.NOT_MATCHED_AUTHNUM);
        }

        member.updateEmail(dto.getEmail());

        memberRepository.save(member);
        mailRedisService.deleteData(dto.getAuthNum());
    }

    public void changePassword(String memberId, MemberChangePasswordDto dto) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(dto.getCurPassword(), member.getPassWord())) {
            throw new MemberException(MemberErrorCode.NOT_MATCHED_PASSWORD);
        }

        if (passwordEncoder.matches(dto.getNewPassword(), member.getPassWord())) {
            throw new MemberException(MemberErrorCode.DUPLICATED_PASSWORD);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new MemberException(MemberErrorCode.NOT_MATCHED_CHECK_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(dto.getNewPassword()));

        memberRepository.save(member);
    }

    public String findMemberId(MemberFindIdDto dto) {
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return member.getMemberId();
    }

    public void findPassword(MemberFindPasswordDto dto) {
        if (!memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        String newPassword = UUID.randomUUID().toString().substring(0, 8);

        member.updatePassword(passwordEncoder.encode(newPassword));
        mailPasswordService.sendMessage(member.getEmail(), newPassword);

        memberRepository.save(member);
    }

    @Transactional
    public void logout(String memberId, HttpServletRequest request, HttpServletResponse response) {

        log.info("logout시 emitter 삭제");
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        Map<String, SseEmitter> emitters =
            emitterRepository.findAllEmitterStartWithMemberId(String.valueOf(member.getId()));
        log.info("emitters.keySet(): {}", emitters.keySet());

        emitters.forEach(
            (emitterId, emitter) -> {
                log.info("삭제하려는 emitterId: {}", emitterId);
                log.info("삭제하려는 emitter: {}", emitter);
                emitter.complete();
            }
        );

        String accessToken = tokenProvider.resolveTokenFromRequest(request);

        long accessTokenExpiration = tokenProvider.getExpiration(accessToken);

        CookieUtil.deleteCookie(request, response, "refreshToken");

        refreshTokenService.deleteRefreshTokenByAccessToken(accessToken);
        refreshTokenService.setBlackList(accessToken, "accessToken",
            accessTokenExpiration);

        // 보안 컨텍스트에서 인증 정보 제거
        SecurityContextHolder.clearContext();
    }

    public void withdrawal(String memberId, String withDrawalReason) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        if (auctionRepository.existsByMemberIdAndAuctionState(
            member.getMemberId(), AuctionState.CONTINUE)) {
            throw new IllegalStateException("진행 중인 경매가 존재하여 회원탈퇴를 진행할 수 없습니다.");
        }

        if (transactionRepository.existsByUser(member.getMemberId(),
            TransType.CONTINUE)) {
            throw new IllegalStateException("진행 중인 거래가 존재하여 회원탈퇴를 진행할 수 없습니다.");
        }

        List<Bid> bidList = bidRepository.findAllByMemberPk(member.getId());
        if (bidList != null && !bidList.isEmpty()) {
            bidRepository.deleteAll(bidList);
        }

        member.withdrawal(withDrawalReason);

        memberRepository.save(member);
    }
}
