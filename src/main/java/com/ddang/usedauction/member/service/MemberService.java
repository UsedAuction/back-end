package com.ddang.usedauction.member.service;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.domain.enums.Role;
import com.ddang.usedauction.member.dto.*;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.exception.MemberErrorCode;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.ddang.usedauction.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
@Getter
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WelcomeMailService welcomeMailService;
    @Value("${spring.jwt.access.expiration}")
    private Long accessExpiration;
    @Value("${spring.jwt.refresh.expiration}")
    private long refreshExpiration;

    private final MailService mailService;
    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;


    private final String IS_EMAIL_AUTH = ":isAuth";


    /**
     * 이메일 인증코드 전송
     *
     * @param email
     */
    public void sendCodeToEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.EXIST_EMAIL);
        }
        String title = "땅땅땅 이메일 인증 번호";
        String authCode = this.createCode();
        mailService.sendEmail(email, title, authCode);
        // 이메일 인증 요청 시 인증 번호 Redis에 저장
        redisTemplate.opsForValue().set(email, authCode); // redisTemplate 에 저장
        redisTemplate.opsForValue().set(email + IS_EMAIL_AUTH, false); // 인증 진행 여부
    }

    public void verifyCode(VerifyCodeDto.Request request) {
        String email = request.getEmail();
        String code = request.getCode();
        String originalCode = (String) redisTemplate.opsForValue().get(email);
        if (!code.equals(originalCode)) { // 인증 코드를 올바르지 않게 작성한 경우
            throw new MemberException(MemberErrorCode.NOT_MATCH_CODE);
        }

        redisTemplate.opsForValue().set(email + IS_EMAIL_AUTH, true); // 인증 여부 변경
    }

    /**
     * 이메일 인증을 위한 인증코드 생성 메서드
     *
     * @return 생성한 인증코드를 리턴
     */
    private String createCode() {
        SecureRandom secureRandom = new SecureRandom();  // SecureRandom 인스턴스를 사용
        StringBuilder sb = new StringBuilder();

        IntStream.range(0, 4)
                .forEach(i -> {
                    int randomNum = secureRandom.nextInt(10); // 0~9 까지 랜덤 숫자
                    sb.append(randomNum);
                });

        return sb.toString();
    }

    public void signup(MemberSignupDto.Request request) {
        String email = request.getEmail();
        String memberId = request.getMemberId();
        String password = request.getPassWord();
        String confirmPassword = request.getConfirmPassWord();

        // 이메일 인증 상태 확인
        if (Boolean.FALSE.equals(redisTemplate.hasKey(email + IS_EMAIL_AUTH))
                || Boolean.FALSE.equals(
                redisTemplate.opsForValue().get(email + IS_EMAIL_AUTH))) {
            throw new MemberException(MemberErrorCode.NOT_AUTH_OF_MAIL);
        }

        // 인증 코드 삭제 (옵션)
        redisTemplate.delete(email);

        // 아이디 중복 확인
        if (memberRepository.existsByMemberId(memberId)) {
            throw new MemberException(MemberErrorCode.EXIST_USER_ID);
        }

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.EXIST_EMAIL);
        }

        // 비밀번호 일치 여부 확인
        if (!password.equals(confirmPassword)) {
            throw new MemberException(MemberErrorCode.NOT_MATCH_PASSWORD);
        }

        // 비밀번호 인코딩
        String encodedPw = passwordEncoder.encode(password);

        // 회원 생성 및 저장
        Member member = Member.builder()
                .memberId(memberId)
                .passWord(encodedPw)
                .email(email)
                .role(Role.USER)
                .build();

        memberRepository.save(member);

        // 환영 이메일 전송
        sendWelcomeEmail(member);
    }

    private void sendWelcomeEmail(Member member) {
        String fromEmail = "seungh2206@gmail.com";
        String fromName = "관리자";
        String toEmail = member.getEmail();
        String toName = member.getMemberId();
        String title = "[땅땅땅!] 회원가입을 축하드립니다.";
        String contents = "땅땅땅! 중고물품 거래 웹사이트 회원가입을 축하드립니다.";
        welcomeMailService.sendMail(fromEmail, fromName, toEmail, toName, title, contents);
    }

    /**
     * 로그인 -> 회원아이디랑 accesstoken 프론트에 넘겨줘야 함
     */
    public TokenDto loginAndGenerateToken(MemberLoginDto.Request request, HttpServletResponse response) {

        String memberId = request.getMemberId();
        String passWord = request.getPassWord();

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        if (!passwordEncoder.matches(passWord, member.getPassWord())) {
            throw new MemberException(MemberErrorCode.NOT_MATCH_PASSWORD);
        }

        // 사용자 정보 가져오기
        String userId = member.getMemberId();
        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(member.getRole().toString()));

        // JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.generateToken(userId, authorities);

        // JWT 쿠키의 만료 시간 설정
        int maxAge = (int) refreshExpiration;

        refreshTokenService.save();

        // Access Token을 쿠키에 저장
        CookieUtil.addCookie(response, "refreshToken", tokenDto.getRefreshToken(), maxAge);

        return tokenDto;
    }

    /**
     * 로그아웃
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 요청에서 JWT 토큰을 추출합니다.
        String token = CookieUtil.getCookieValue(request, "JWT")
                .orElse(null);

        // JWT 토큰이 존재하고 유효할 경우
        if (token != null && tokenProvider.validateToken(token)) {
            // 토큰의 남은 유효 시간을 계산합니다.
            long expiration = tokenProvider.getExpiration(token);

            // 블랙리스트에 추가합니다.
            redisTemplate.opsForValue().set(token, "logout", expiration, TimeUnit.MILLISECONDS);
        }

        // JWT 쿠키를 삭제합니다.
        CookieUtil.deleteCookie(request, response, "JWT");
    }


    /**
     * 회원 탈퇴
     */
    public void deleteMemberAndLogout(String memberId, HttpServletRequest request, HttpServletResponse response) {
        // 사용자 정보 조회 및 삭제
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        memberRepository.delete(member);

        // 로그아웃 처리
        logout(request, response);
    }

    // 이메일 인증 여부 확인
    private boolean isEmailAuthenticated(String email) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().get(email + IS_EMAIL_AUTH));
    }

    // 이메일 인증 정보 삭제
    private void clearEmailAuth(String email) {
        redisTemplate.delete(email + IS_EMAIL_AUTH);
    }

    // 사용자 아이디가 이미 존재하는지 확인
    private boolean isUserIdExists(String memberId) {
        return memberRepository.existsByMemberId(memberId);
    }

    // 이메일이 이미 존재하는지 확인
    private boolean isEmailExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 회원 정보 조회
     */
    public MemberServiceDto getMemberProfile(String userId) {
        Member member = memberRepository.findByMemberId(userId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        return member.toServiceDto();
    }

    /**
     * 회원 정보 수정
     */

    /**
     * 회원 ID 수정: 이메일과 같으면 안 된다.
     */
    public MemberServiceDto updateMemberId(Long id, MemberIdUpdateDto.Request request) {

        // id를 기준으로 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String updatedMemberId = request.getMemberId();

        // 이메일과 같은지 확인
        if (updatedMemberId.equals(member.getEmail())) {
            throw new MemberException(MemberErrorCode.MEMBER_ID_EQUALS_EMAIL);
        }

        // 아이디 중복 확인
        if (memberRepository.existsByMemberId(updatedMemberId)) {
            throw new MemberException(MemberErrorCode.EXIST_USER_ID);
        }

        // member 객체의 memberId를 업데이트
        Member updatedMember = member.toBuilder()
                .memberId(updatedMemberId)
                .build();

        Member save = memberRepository.save(updatedMember);

        return save.toServiceDto();
    }

    /**
     * 비밀번호 수정: 현재 비밀번호와 새 비밀번호, 새 비밀번호 확인 일치해야 함
     */
    public MemberServiceDto updatePassword(Long id, UpdatePasswordDto.Request request) {

        // 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String confirmPassWord = request.getPassWord();
        String updatedPassWord = request.getNewPassWord();
        String confirmUpdatedPassWord = request.getConfirmNewPassword();


        // 현재 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(confirmPassWord, member.getPassWord())) {
            throw new MemberException(MemberErrorCode.NOT_MATCH_PASSWORD);
        }

        // 새 비밀번호와 확인 비밀번호가 일치하는지 확인
        if (!updatedPassWord.equals(confirmUpdatedPassWord)) {
            throw new MemberException(MemberErrorCode.NOT_MATCH_NEWPASSWORD);
        }
        String encodedPassword = passwordEncoder.encode(updatedPassWord);

        Member updatedMember = member.toBuilder()
                .passWord(encodedPassword)
                .build();

        Member save = memberRepository.save(updatedMember);

        return save.toServiceDto();
    }

    /**
     * 이메일 수정: 인증 코드 발송
     */
    public void sendEmailUpdateCode(String newEmail) {
        if (memberRepository.existsByEmail(newEmail)) {
            throw new MemberException(MemberErrorCode.EXIST_EMAIL);
        }

        String title = "땅땅땅 이메일 인증 번호";
        String authCode = createCode();
        mailService.sendEmail(newEmail, title, authCode);

        redisTemplate.opsForValue().set(newEmail, authCode);
        redisTemplate.opsForValue().set(newEmail + IS_EMAIL_AUTH, false);
    }

    /**
     * 이메일 수정: 인증 번호 확인 및 이메일 변경
     */
    public void updateEmail(Long id, VerifyCodeDto.Request request) {
        String newEmail = request.getEmail();
        String code = request.getCode();

        // Redis에서 저장된 인증번호 가져오기
        String originalCode = (String) redisTemplate.opsForValue().get(newEmail);

        if (!code.equals(originalCode)) {
            throw new MemberException(MemberErrorCode.NOT_MATCH_CODE);
        }

        // 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        // 이메일 중복 확인
        if (memberRepository.existsByEmail(newEmail)) {
            throw new MemberException(MemberErrorCode.EXIST_EMAIL);
        }

        // 이메일 업데이트
        Member updatedMember = member.toBuilder()
                .email(newEmail)
                .build();

        memberRepository.save(updatedMember);

        // 인증 성공 처리
        redisTemplate.opsForValue().set(newEmail + IS_EMAIL_AUTH, true);
        redisTemplate.delete(newEmail);  // 인증 코드 삭제
    }

}

