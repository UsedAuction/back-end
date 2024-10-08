package com.ddang.usedauction.member.servie;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.repository.AuctionRepository;
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
import com.ddang.usedauction.member.dto.MemberWithdrawalDto;
import com.ddang.usedauction.member.exception.MemberErrorCode;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.dto.TokenDto;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import jakarta.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    MemberRepository memberRepository;

    @Mock
    AuctionRepository auctionRepository;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    BidRepository bidRepository;

    @Mock
    MailRedisService mailRedisService;

    @Mock
    MailPasswordService mailPasswordService;

    @Mock
    TokenProvider tokenProvider;

    @Mock
    RefreshTokenService refreshTokenService;

    MockHttpServletRequest request;

    @Mock
    MockHttpServletResponse response;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("회원 정보 조회")
    void getMember() {

        Member member = Member.builder()
            .memberId("test")
            .email("test@naver.com")
            .point(0)
            .id(1L)
            .siteAlarm(true)
            .passWord("1234")
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.of(member));

        MemberGetDto.Response result = memberService.getMember("test");

        assertEquals("test", result.getMemberId());
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 없는 회원")
    void getMemberFail1() {

        when(memberRepository.findByMemberIdAndDeletedAtIsNull("test")).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.getMember("test"));
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login() throws Exception {
        MemberLoginRequestDto dto = MemberLoginRequestDto.builder()
            .memberId("test1234")
            .password("QWER12")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        TokenDto token = TokenDto.builder()
            .accessToken("accessToken")
            .refreshToken("refreshToken")
            .build();

        long refreshTokenExpiration = 8000L;

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            Optional.of(member));
        when(passwordEncoder.matches(dto.getPassword(), member.getPassWord())).thenReturn(true);
        when(tokenProvider.generateToken(member.getMemberId(),
            Collections.singletonList(
                new SimpleGrantedAuthority(member.getRole().toString())))).thenReturn(token);
        when(tokenProvider.getExpiration(token.getRefreshToken())).thenReturn(
            refreshTokenExpiration);

        MemberLoginResponseDto responseDto = memberService.login(response, dto);

        verify(memberRepository, times(1)).findByMemberIdAndDeletedAtIsNull(member.getMemberId());
        verify(passwordEncoder, times(1)).matches(dto.getPassword(), member.getPassWord());
        verify(tokenProvider, times(1)).generateToken(member.getMemberId(),
            Collections.singletonList(new SimpleGrantedAuthority(member.getRole().toString())));
        verify(refreshTokenService, times(1)).save(token.getAccessToken(), token.getRefreshToken(),
            (int) refreshTokenExpiration);

        assertEquals(token.getAccessToken(), responseDto.getAccessToken());
        assertEquals(member.getMemberId(), responseDto.getMemberId());
    }

    @Test
    @DisplayName("로그인 - 실패: 존재하지 않는 아이디")
    void loginFail1() throws Exception {
        MemberLoginRequestDto dto = MemberLoginRequestDto.builder()
            .memberId("test1234")
            .password("QWER12")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.login(response, dto));

        verify(memberRepository, times(1)).findByMemberIdAndDeletedAtIsNull(member.getMemberId());

    }

    @Test
    @DisplayName("로그인 - 실패: 패스워드 불일치")
    void loginFail2() throws Exception {
        MemberLoginRequestDto dto = MemberLoginRequestDto.builder()
            .memberId("test1234")
            .password("QWER12")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            Optional.of(member));
        when(passwordEncoder.matches(dto.getPassword(), member.getPassWord())).thenReturn(false);

        assertThrows(MemberException.class, () -> memberService.login(response, dto));
        verify(memberRepository, times(1)).findByMemberIdAndDeletedAtIsNull(dto.getMemberId());
        verify(passwordEncoder, times(1)).matches(dto.getPassword(), member.getPassWord());

    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signup() throws Exception {

        MemberSignUpDto dto = MemberSignUpDto.builder()
            .memberId("test1234")
            .password("pwdQW12")
            .confirmPassword("pwdQW12")
            .email("saab35@naver.com")
            .authNum("1234")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("QWER1234")
            .email("saab35@naver.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            false);
        when(memberRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(mailRedisService.getData("1234")).thenReturn("saab35@naver.com");
        when(memberRepository.save(argThat(arg -> arg.getMemberId().equals("test1234"))))
            .thenReturn(member);

        memberService.signUp(dto);

        verify(memberRepository, times(1)).save(
            argThat(arg -> arg.getMemberId().equals("test1234")));
        verify(memberRepository, times(1)).save(
            argThat(arg -> arg.getEmail().equals("saab35@naver.com")));
    }

    @Test
    @DisplayName("회원가입 - 실패: 인증번호 불일치")
    void signupFail1() throws Exception {
        MemberSignUpDto dto = MemberSignUpDto.builder()
            .memberId("test1234")
            .password("pwdQW12")
            .confirmPassword("pwdQW12")
            .email("saab35@naver.com")
            .authNum("1234")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("QWER1234")
            .email("saab35@naver.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            false);
        when(memberRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())).thenReturn(false);
        when(mailRedisService.getData("1234")).thenReturn("wrong@email.com");

        assertThrows(MemberException.class, () -> memberService.signUp(dto));

        verify(memberRepository, times(0)).save(member);
    }

    @Test
    @DisplayName("회원가입 - 실패: 이미 존재하는 아이디")
    void signupFail2() throws Exception {
        MemberSignUpDto dto = MemberSignUpDto.builder()
            .memberId("test1234")
            .password("pwdQW12")
            .confirmPassword("pwdQW12")
            .email("saab35@naver.com")
            .authNum("1234")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("QWER1234")
            .email("saab35@naver.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            true);

        assertThrows(MemberException.class, () -> memberService.signUp(dto));

        verify(memberRepository, times(0)).save(member);
    }

    @Test
    @DisplayName("회원가입 - 실패: 이미 존재하는 이메일")
    void signupFail3() throws Exception {
        MemberSignUpDto dto = MemberSignUpDto.builder()
            .memberId("test1234")
            .password("pwdQW12")
            .confirmPassword("pwdQW12")
            .email("saab35@naver.com")
            .authNum("1234")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("QWER1234")
            .email("saab35@naver.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            false);
        when(memberRepository.existsByEmailAndDeletedAtIsNull(dto.getEmail())).thenReturn(true);

        assertThrows(MemberException.class, () -> memberService.signUp(dto));

        verify(memberRepository, times(0)).save(member);
    }

    @Test
    @DisplayName("회원가입 - 실패: 비밀번호 불일치")
    void signupFail4() throws Exception {
        MemberSignUpDto dto = MemberSignUpDto.builder()
            .memberId("test1234")
            .password("pwdQW12")
            .confirmPassword("wqeqwe1123qQQ")
            .email("saab35@naver.com")
            .authNum("1234")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("QWER1234")
            .email("saab35@naver.com")
            .role(Role.ROLE_USER)
            .build();

        assertThrows(MemberException.class, () -> memberService.signUp(dto));

        verify(memberRepository, times(0)).save(member);
    }

    @Test
    @DisplayName("아이디 중복 확인 - 성공")
    void checkMemberId() throws Exception {
        MemberCheckIdDto dto = MemberCheckIdDto.builder()
            .memberId("existingID")
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            false);

        assertDoesNotThrow(() -> memberService.checkMemberId(dto));
    }

    @Test
    @DisplayName("아이디 중복 확인 - 실패: 이미 존재하는 아이디")
    void checkMemberIdFail() throws Exception {
        MemberCheckIdDto dto = MemberCheckIdDto.builder()
            .memberId("existingID")
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            true);

        MemberException exception = assertThrows(MemberException.class,
            () -> memberService.checkMemberId(dto));

        assertEquals(MemberErrorCode.ALREADY_EXISTS_MEMBER_ID, exception.getErrorCode());
        assertEquals("이미 존재하는 아이디입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("이메일 변경 - 성공")
    void changeEmail() throws Exception {
        MemberChangeEmailDto dto = MemberChangeEmailDto.builder()
            .email("newEmail@email.com")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword")
            .email("oldEmail@email.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.of(member));
        when(mailRedisService.getData(dto.getAuthNum())).thenReturn("newEmail@email.com");

        assertDoesNotThrow(() -> memberService.changeEmail(member.getMemberId(), dto));

        verify(memberRepository, times(1)).save(member);
        assertEquals(dto.getEmail(), member.getEmail());
    }

    @Test
    @DisplayName("이메일 변경 - 실패: 존재하지 않는 회원")
    void changeEmailFail1() throws Exception {
        MemberChangeEmailDto dto = MemberChangeEmailDto.builder()
            .email("newEmail@email.com")
            .build();
        String memberId = "test1234";

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.changeEmail(memberId, dto));
    }

    @Test
    @DisplayName("이메일 변경 - 실패: 동일한 이메일 변경 시도")
    void changeEmailFail2() throws Exception {
        MemberChangeEmailDto dto = MemberChangeEmailDto.builder()
            .email("sameEmail@email.com")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword")
            .email("sameEmail@email.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.of(member));

        MemberException exception = assertThrows(MemberException.class,
            () -> memberService.changeEmail(member.getMemberId(), dto));

        assertEquals(MemberErrorCode.DUPLICATED_EMAIL, exception.getErrorCode());
    }

    @Test
    @DisplayName("이메일 변경 - 실패: 이메일 인증번호 불일치")
    void changeEmailFail3() throws Exception {
        MemberChangeEmailDto dto = MemberChangeEmailDto.builder()
            .email("newEmail@email.com")
            .authNum("1234")
            .build();

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword")
            .email("oldEmail@email.com")
            .role(Role.ROLE_USER)
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.of(member));
        when(mailRedisService.getData(dto.getAuthNum())).thenReturn("wrong@email.com");

        MemberException exception = assertThrows(MemberException.class,
            () -> memberService.changeEmail(member.getMemberId(), dto));
        assertEquals(MemberErrorCode.NOT_MATCHED_AUTHNUM, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void changePassword() throws Exception {
        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword1")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        MemberChangePasswordDto dto = MemberChangePasswordDto.builder()
            .curPassword("oldPassword1")
            .newPassword("newPassword1")
            .confirmPassword("newPassword1")
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.of(member));
        when(passwordEncoder.matches(dto.getCurPassword(), member.getPassWord())).thenReturn(true);
        when(passwordEncoder.encode(dto.getNewPassword())).thenReturn("encodedNewPassword1");

        memberService.changePassword(member.getMemberId(), dto);

        verify(memberRepository, times(1)).save(member);
        verify(passwordEncoder, times(1)).encode(dto.getNewPassword());
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패: 존재하지 않는 회원")
    void changePasswordFail1() throws Exception {
        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword1")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        MemberChangePasswordDto dto = MemberChangePasswordDto.builder()
            .curPassword("oldPassword1")
            .newPassword("newPassword1")
            .confirmPassword("newPassword1")
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> memberService.changePassword(member.getMemberId(), dto));

        verify(memberRepository, times(0)).save(member);
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패: 현재 비밀번호 불일치")
    void changePasswordFail2() throws Exception {
        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword1")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        MemberChangePasswordDto dto = MemberChangePasswordDto.builder()
            .curPassword("oldPassword1")
            .newPassword("newPassword1")
            .confirmPassword("newPassword1")
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.of(member));
        when(passwordEncoder.matches(dto.getCurPassword(), member.getPassWord())).thenReturn(
            false);

        assertThrows(MemberException.class,
            () -> memberService.changePassword(member.getMemberId(), dto),
            MemberErrorCode.NOT_MATCHED_PASSWORD.getMessage());

        verify(memberRepository, times(0)).save(member);
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패: 새로운 비밀번호와 확인 비밀번호 불일치")
    void changePasswordFail3() throws Exception {
        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword1")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        MemberChangePasswordDto dto = MemberChangePasswordDto.builder()
            .curPassword("oldPassword1")
            .newPassword("newPassword1")
            .confirmPassword("wrongPassword1")
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.of(member));
        when(passwordEncoder.matches(dto.getCurPassword(), member.getPassWord())).thenReturn(true);

        assertThrows(MemberException.class, () -> memberService.changePassword("test1234", dto),
            MemberErrorCode.NOT_MATCHED_CHECK_PASSWORD.getMessage());

        verify(memberRepository, times(0)).save(member);
    }


    @Test
    @DisplayName("아이디 찾기 - 성공")
    void findMemberId() throws Exception {
        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword1")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        MemberFindIdDto dto = MemberFindIdDto.builder()
            .email("test@email.com")
            .build();

        when(memberRepository.findByEmailAndDeletedAtIsNull(member.getEmail())).thenReturn(
            Optional.of(member));

        String findMemberId = memberService.findMemberId(dto);

        assertEquals(findMemberId, member.getMemberId());
    }

    @Test
    @DisplayName("아이디 찾기 - 실패: 이메일로 가입된 아이디 없음")
    void findMemberIdFail() throws Exception {
        MemberFindIdDto dto = MemberFindIdDto.builder()
            .email("test@email.com")
            .build();

        when(memberRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.findMemberId(dto),
            "가입된 아이디가 존재하지 않습니다,");
    }

    @Test
    @DisplayName("비밀번호 찾기 - 성공")
    void findPassword() throws Exception {
        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword1")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        MemberFindPasswordDto dto = MemberFindPasswordDto.builder()
            .memberId("test1234")
            .email("test@email.com")
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            true);
        when(memberRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())).thenReturn(
            Optional.of(member));
        when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");

        memberService.findPassword(dto);

        verify(memberRepository, times(1)).save(member);
        verify(passwordEncoder, times(1)).encode(any());
        verify(mailPasswordService, times(1)).sendMessage(
            argThat(email -> email.equals(dto.getEmail())), any());
        verify(memberRepository, times(1)).save(member);

    }

    @Test
    @DisplayName("비밀번호 찾기 - 실패: 존재하지 않는 회원 아이디")
    void findPasswordFail() throws Exception {

        MemberFindPasswordDto dto = MemberFindPasswordDto.builder()
            .memberId("test1234")
            .email("test@email.com")
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            true);

        assertThrows(NoSuchElementException.class, () -> memberService.findPassword(dto));

        verify(memberRepository, times(1)).existsByMemberIdAndDeletedAtIsNull(dto.getMemberId());
        verify(memberRepository, times(0)).existsByEmailAndDeletedAtIsNull(dto.getEmail());
    }

    @Test
    @DisplayName("비밀번호 찾기 - 실패: 존재하지 않는 이메일")
    void findPasswordFail2() throws Exception {

        MemberFindPasswordDto dto = MemberFindPasswordDto.builder()
            .memberId("test1234")
            .email("test@email.com")
            .build();

        when(memberRepository.existsByMemberIdAndDeletedAtIsNull(dto.getMemberId())).thenReturn(
            true);
        when(memberRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.findPassword(dto));

        verify(memberRepository, times(1)).existsByMemberIdAndDeletedAtIsNull(dto.getMemberId());
        verify(memberRepository, times(1)).findByEmailAndDeletedAtIsNull(dto.getEmail());
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout() throws Exception {
        String memberId = "test1234";
        String accessToken = "token";
        long accessTokenExpiration = 3600L;

        request = new MockHttpServletRequest();
        Cookie[] cookies = new Cookie[]{new Cookie("refreshToken", "dummy")};
        request.setCookies(cookies);

        when(tokenProvider.resolveTokenFromRequest(request)).thenReturn(accessToken);
        when(tokenProvider.getExpiration(accessToken)).thenReturn(accessTokenExpiration);

        memberService.logout(memberId, request, response);

        verify(tokenProvider, times(1)).resolveTokenFromRequest(request);
        verify(tokenProvider, times(1)).getExpiration(accessToken);
        verify(refreshTokenService, times(1)).deleteRefreshTokenByAccessToken(accessToken);
        verify(refreshTokenService, times(1)).setBlackList(accessToken, "accessToken",
            accessTokenExpiration);
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void withdrawal() {

        Member member = Member.builder()
            .id(1L)
            .memberId("test1234")
            .passWord("encodedPassword1")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        MemberWithdrawalDto dto = MemberWithdrawalDto.builder()
            .withDrawalReason("개인정보 보호")
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.of(member));
        when(auctionRepository.existsByMemberIdAndAuctionState(
            member.getMemberId(), AuctionState.CONTINUE)).thenReturn(false);
        when(transactionRepository.existsByUser(member.getMemberId(),
            TransType.CONTINUE)).thenReturn(false);
        when(bidRepository.findAllByMemberPk(1L)).thenReturn(new ArrayList<>());

        memberService.withdrawal(member.getMemberId(), dto.getWithDrawalReason());

        verify(memberRepository, times(1)).save(member);
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패")
    void withdrawalFail() {

        Member member = Member.builder()
            .memberId("test1234")
            .passWord("encodedPassword1")
            .email("test@email.com")
            .role(Role.ROLE_USER)
            .build();

        MemberWithdrawalDto dto = MemberWithdrawalDto.builder()
            .withDrawalReason("개인정보 보호")
            .build();

        when(memberRepository.findByMemberIdAndDeletedAtIsNull(member.getMemberId())).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> memberService.withdrawal(member.getMemberId(), dto.getWithDrawalReason()));

        verify(memberRepository, times(1)).findByMemberIdAndDeletedAtIsNull(member.getMemberId());
        verify(memberRepository, never()).save(member);
    }
}