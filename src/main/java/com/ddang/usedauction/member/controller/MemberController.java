package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.component.MailComponent;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.exception.IllegalMemberAccessException;
import com.ddang.usedauction.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/members")
@Controller
public class MemberController {
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final MailComponent mailComponent;

    public MemberController(MemberService memberService, MailComponent mailComponent) {
        this.memberService = memberService;
        this.mailComponent = mailComponent;
    }

    /**
     * 로그인 기능
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String memberId = loginData.get("memberId");
        String passWord = loginData.get("passWord");

        Member member = memberService.findByMemberId(memberId);
        if (!member.isValidPassWord(passWord)) {
            throw new IllegalMemberAccessException("비밀번호가 틀렸습니다.");
        }

        session.setAttribute(HttpSessionUtils.MEMBER_SESSION_KEY, member);
        logger.debug("member : {}님이 로그인하셨습니다.", member.getMemberId());

        return ResponseEntity.ok("로그인이 완료되었습니다!");
    }

    /**
     * 로그아웃 기능
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        Member member = HttpSessionUtils.getSessionMember(session);
        if (member != null) {
            logger.debug("member : {}님이 로그아웃하셨습니다.", member.getMemberId());
            session.removeAttribute(HttpSessionUtils.MEMBER_SESSION_KEY);
            return ResponseEntity.ok("로그아웃이 완료되었습니다!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그인된 사용자가 없습니다.");
        }
    }

    /**
     * 회원가입 기능
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Member member) {
        memberService.create(member);
        logger.debug("member : {}님이 가입하셨습니다.", member.getMemberId());

        //메일 전송

        String fromEmail = "seungh22@gmail.com";
        String fromName = "관리자";
        String toEmail = member.getEmail();
        String toName = member.getMemberId();

        String title = "[땅땅땅!] 회원가입을 축하드립니다.";
        String contents = "땅땅땅! 중고 물품 거래 웹사이트 회원가입을 축하드립니다.";

        mailComponent.send(fromEmail, fromName, toEmail, toName, title, contents);

        return ResponseEntity.ok("회원가입이 완료되었습니다!");
    }

    /**
     * 회원탈퇴 기능
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id, HttpSession session) {
        Member member = memberService.findVerifiedMember(id, session);

        memberService.deleteMember(id);
        session.removeAttribute(HttpSessionUtils.MEMBER_SESSION_KEY); // 세션에서 해당 회원 정보 삭제
        logger.debug("member : {}님이 탈퇴하셨습니다.", member.getMemberId());

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다!");
    }

    /**
     * 특정 회원의 프로필을 JSON 형태로 반환합니다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> profile(@PathVariable Long id) {
        Member member = memberService.findMember(id);
        return ResponseEntity.ok(member);
    }

    /**
     * 회원 정보 수정 기능
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> updateData, HttpSession session) {
        Member member = memberService.findVerifiedMember(id, session);

        String password = updateData.get("password");
        if (!member.isValidPassWord(password)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호가 일치하지 않습니다.");
        }

        // memberId 업데이트
        String newMemberId = updateData.get("memberId");
        if (newMemberId != null && !newMemberId.isEmpty()) {
            member.setMemberId(newMemberId);
        }

        // email 업데이트
        member.setEmail(updateData.get("email"));

        memberService.update(member, member);

        session.setAttribute(HttpSessionUtils.MEMBER_SESSION_KEY, member);
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다!");
    }
}
