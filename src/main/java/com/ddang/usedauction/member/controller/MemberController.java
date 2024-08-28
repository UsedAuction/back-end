package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.dto.MemberChangeEmailDto;
import com.ddang.usedauction.member.dto.MemberChangePasswordDto;
import com.ddang.usedauction.member.dto.MemberCheckIdDto;
import com.ddang.usedauction.member.dto.MemberFindIdDto;
import com.ddang.usedauction.member.dto.MemberFindPasswordDto;
import com.ddang.usedauction.member.dto.MemberLoginRequestDto;
import com.ddang.usedauction.member.dto.MemberLoginResponseDto;
import com.ddang.usedauction.member.dto.MemberSignUpDto;
import com.ddang.usedauction.member.servie.MemberService;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponseDto> login(
        HttpServletResponse response, @RequestBody @Valid MemberLoginRequestDto dto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(memberService.login(response, dto));
    }

    @GetMapping("/check/id")
    public ResponseEntity<String> checkMemberId(@RequestBody @Valid MemberCheckIdDto dto) {
        memberService.checkMemberId(dto);
        return ResponseEntity.status(HttpStatus.OK)
            .body("사용 가능한 아이디 입니다.");
    }


    @PostMapping("/signup")
    public ResponseEntity<String> singUp(@RequestBody @Valid MemberSignUpDto dto) {
        memberService.signUp(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("회원가입이 완료되었습니다.");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal PrincipalDetails principalDetails
        , HttpServletRequest request, HttpServletResponse response) {
        memberService.logout(principalDetails.getName(), request, response);
        return ResponseEntity.status(HttpStatus.OK)
            .body("로그아웃 되었습니다");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/withdrawl")
    public ResponseEntity<String> withdrawl(
        @AuthenticationPrincipal PrincipalDetails principalDetails) {
        memberService.withdrawal(principalDetails.getName());

        return ResponseEntity.status(HttpStatus.OK)
            .body("회원 탈퇴가 완료되었습니다.");
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/change/email")
    public ResponseEntity<String> changeEmail(
        @AuthenticationPrincipal PrincipalDetails principalDetails
        , @RequestBody @Valid MemberChangeEmailDto dto) {
        memberService.changeEmail(principalDetails.getName(), dto);
        return ResponseEntity.status(HttpStatus.OK)
            .body("이메일이 변경되었습니다.");
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/change/password")
    public ResponseEntity<String> changePassword(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @RequestBody @Valid MemberChangePasswordDto dto) {

        memberService.changePassword(principalDetails.getName(), dto);
        return ResponseEntity.status(HttpStatus.OK)
            .body("비밀번호가 변경되었습니다.");
    }

    @PostMapping("/find/id")
    public ResponseEntity<String> findMemberId(@RequestBody @Valid MemberFindIdDto dto) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(memberService.findMemberId(dto));
    }

    @PostMapping("/find/password")
    public ResponseEntity<String> findPassword(@RequestBody @Valid MemberFindPasswordDto dto) {
        memberService.findPassword(dto);
        return ResponseEntity.status(HttpStatus.OK)
            .body("비밀번호가 재발급되었습니다.");
    }


}
