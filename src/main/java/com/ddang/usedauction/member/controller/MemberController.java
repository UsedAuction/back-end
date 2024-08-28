package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.dto.*;
import com.ddang.usedauction.member.service.MemberService;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.dto.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    @Value("${spring.jwt.access.expiration}")
    private Long accessExpiration;

    /**
     * 인증코드 메일 발송
     *
     * @param email 메일 받을 이메일
     * @return 메일 전송 여부
     */
    @PostMapping("/auth/{email}")
    public ResponseEntity<String> sendEmailCode(
            @NotBlank(message = "이메일을 입력해주세요.") @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "올바른 이메일을 입력해주세요.") @PathVariable String email) {

        memberService.sendCodeToEmail(email);

        return ResponseEntity.ok("인증코드가 이메일로 전송되었습니다.");
    }

    /**
     * 인증코드 확인
     *
     * @param request 인증코드 받은 이메일과 입력한 인증코드
     * @return 인증코드 매칭 여부
     */
    @PostMapping("/auth/email")
    public ResponseEntity<String> verifyEmailCode(
            @Valid @RequestBody VerifyCodeDto.Request request) {

        memberService.verifyCode(request);

        return ResponseEntity.ok("인증코드가 성공적으로 확인되었습니다.");
    }


    /**
     * 회원가입 컨트롤러
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody MemberSignupDto.Request request) {
        memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    /**
     * 로그인 컨트롤러
     */
    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@Valid @RequestBody MemberLoginDto.Request request, HttpServletResponse response) {
        // 서비스 계층으로 로그인 요청 처리 위임
        TokenDto tokenDto = memberService.loginAndGenerateToken(request, response);
        return ResponseEntity.ok("로그인에 성공했습니다. Access Token: " + tokenDto.getAccessToken());
    }

    /**
     * 로그아웃 컨트롤러 -> 쿠키 지우고, 블랙리스트 추가
     */
    @PostMapping("/auth/logout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 서비스 계층으로 로그아웃 요청 처리 위임
        memberService.logout(request, response);
        return ResponseEntity.ok("로그아웃에 성공했습니다.");
    }

    /**
     * //     * 회원 탈퇴 컨트롤러
     * //
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteMember(HttpServletRequest request, HttpServletResponse response) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String userId = authentication.getName();

        // 회원 탈퇴 처리
        memberService.deleteMemberAndLogout(userId, request, response);

        return ResponseEntity.ok("회원탈퇴가 완료되었습니다");
    }

    /**
     * 회원 프로필 조회 컨트롤러
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MemberServiceDto> getMemberProfile(@PathVariable String id) {
        MemberServiceDto profile = memberService.getMemberProfile(id);
        return ResponseEntity.ok(profile);
    }

    /**
     * 회원 정보 수정 컨트롤러
     */

    /**
     * 회원 ID 수정: 이메일과 같으면 안 된다.
     */
    @PatchMapping("/update/memberId/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updateMemberId(
            @PathVariable Long id,
            @Valid @RequestBody MemberIdUpdateDto.Request request) {

        MemberServiceDto memberServiceDto = memberService.updateMemberId(id, request);

        MemberIdUpdateDto.Response response = new MemberIdUpdateDto.Response(memberServiceDto.getMemberId());

        return ResponseEntity.ok("아이디가 성공적으로 수정되었습니다.");
    }

    /**
     * 비밀번호 수정: 현재 비밀번호, 새로운 비밀번호, 새로운 비밀번호 확인
     */
    @PatchMapping("/update/password/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePasswordDto.Request request) {

        MemberServiceDto memberServiceDto = memberService.updatePassword(id, request);

        UpdatePasswordDto.Response response = new UpdatePasswordDto.Response(memberServiceDto.getPassWord());

        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    /**
     * 새로운 이메일로 인증번호 전송
     */
    @PostMapping("/update/email/auth/{newEmail}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> sendEmailUpdateCode(
            @NotBlank(message = "이메일을 입력해주세요.")
            @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "올바른 이메일을 입력해주세요.")
            @PathVariable String newEmail) {

        memberService.sendEmailUpdateCode(newEmail);

        return ResponseEntity.ok("이메일 인증코드가 새 이메일로 전송되었습니다.");
    }

    /**
     * 인증번호를 확인하고 이메일을 수정
     */
    @PatchMapping("/update/email/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> updateEmail(
            @PathVariable Long id,
            @Valid @RequestBody VerifyCodeDto.Request request) {

        memberService.updateEmail(id, request);

        return ResponseEntity.ok("이메일이 성공적으로 변경되었습니다.");
    }
}



