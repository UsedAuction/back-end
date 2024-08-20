package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.dto.*;
import com.ddang.usedauction.member.service.MemberService;
import com.ddang.usedauction.config.GlobalApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberService memberService;

    // 하드코딩된 회원 정보 (임시)
    private static final String memberId = "testUser";
    private static final String passWord = "password123";
    private static final String email = "testUser@example.com";

    /**
     * 회원가입 컨트롤러
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<GlobalApiResponse<String>> signup(
            @Valid @RequestBody MemberSignupDto.Request request) {

        MemberServiceDto signup = memberService.signup(request);

        // 하드코딩된 정보가 아닌 경우 (실제 로직에서는 여기서 데이터 저장 로직이 필요)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GlobalApiResponse.toGlobalResponse(HttpStatus.CREATED, signup.getMemberId()));
    }

    /**
     * 로그인 컨트롤러
     */
    @PostMapping("/auth/login")
    public ResponseEntity<GlobalApiResponse<String>> login(
            @Valid @RequestBody MemberLoginDto.Request request, HttpServletResponse response) {

        MemberServiceDto login = memberService.login(request);

        // 하드코딩된 정보와 일치하는지 확인
        if (!request.getMemberId().equals(memberId) || !request.getPassWord().equals(passWord)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(GlobalApiResponse.toGlobalResponse(HttpStatus.UNAUTHORIZED, "잘못된 아이디 또는 비밀번호입니다."));
        }

        // 로그인 성공 시 확인 메시지 반환
        return ResponseEntity.ok(GlobalApiResponse.toGlobalResponse(HttpStatus.OK, "로그인 성공"));
    }

    /**
     * 로그아웃 컨트롤러 (토큰 없이 테스트 가능)
     */
    @PostMapping("/logout")
    public ResponseEntity<GlobalApiResponse<String>> logout() {
        // 로그아웃 성공 처리
        return ResponseEntity.ok(GlobalApiResponse.toGlobalResponse(HttpStatus.OK, "로그아웃이 완료되었습니다!"));
    }

    /**
     * 회원 탈퇴 컨트롤러
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable Long id) {

        // 하드코딩된 회원 정보 삭제 처리 (실제 로직에서는 여기서 데이터 삭제 로직이 필요)
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다!");
    }

    /**
     * 회원 프로필 조회 컨트롤러
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemberGetDto.Response> profile(@PathVariable Long id) {
        // 하드코딩된 회원 정보 반환
        MemberGetDto.Response profile = new MemberGetDto.Response(memberId, passWord);
        return ResponseEntity.ok(profile);
    }

    /**
     * 회원 정보 수정 컨트롤러
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody MemberUpdateDto.Request updateDto) {

        // 업데이트 로직 (임시로 하드코딩된 값이 수정되었다고 가정)
        // 실제 로직에서는 메모리나 데이터베이스에서 해당 데이터를 수정
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다!");
    }
}