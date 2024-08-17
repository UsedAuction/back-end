package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.dto.MemberGetDto.Response;
import com.ddang.usedauction.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.ddang.usedauction.member.domain.Member;


import java.util.Map;

@RequestMapping("/api/members")
@Controller
public class MemberController {
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String memberId = loginData.get("memberId");
        String password = loginData.get("passWord");

        memberService.login(memberId, password, session);
        logger.debug("member : {}님이 로그인하셨습니다.", memberId);

        return ResponseEntity.ok("로그인이 완료되었습니다!");
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        String memberId = memberService.logout(session);
        if (memberId != null) {
            logger.debug("member : {}님이 로그아웃하셨습니다.", memberId);
            return ResponseEntity.ok("로그아웃이 완료되었습니다!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인된 사용자가 없습니다.");
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Member member) {
        memberService.create(member);
        logger.debug("member : {}님이 가입하셨습니다.", member.getMemberId());

        return ResponseEntity.ok("회원가입이 완료되었습니다!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id, HttpSession session) {
        memberService.deleteMember(id, session);
        logger.debug("member : {}님이 탈퇴하셨습니다.", id);

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> profile(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getProfile(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> updateData, HttpSession session) {
        memberService.updateMember(id, updateData, session);
        logger.debug("member : {}님의 정보가 업데이트되었습니다.", id);

        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다!");
    }
}
