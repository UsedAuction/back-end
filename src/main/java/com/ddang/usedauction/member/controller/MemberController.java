package com.ddang.usedauction.member.controller;

import com.ddang.usedauction.member.component.MailComponent;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.dto.MemberGetDto;
import com.ddang.usedauction.member.dto.MemberGetDto.Response;
import com.ddang.usedauction.member.exception.IllegalMemberAccessException;
import com.ddang.usedauction.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/members")
@Controller
public class MemberController {
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;

    public MemberController(MemberService memberService, MailComponent mailComponent, JavaMailSender javaMailSender) {
        this.memberService = memberService;
    }

    /**
     * 로그인 기능
     */
    // 로그인 메서드에서 세션에 저장되는 객체 확인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String memberId = loginData.get("memberId");
        String passWord = loginData.get("passWord");

        Response Member member = memberService.findByMemberId(memberId);
        if (!member.isValidPassWord(passWord)) {
            throw new IllegalMemberAccessException("비밀번호가 틀렸습니다.");
        }

        // 세션에 DTO 저장
        MemberGetDto.Response memberDto = MemberGetDto.Response.from(member);
        session.setAttribute(HttpSessionUtils.MEMBER_SESSION_KEY, memberDto);

        logger.debug("세션에 저장된 객체: {}", session.getAttribute(HttpSessionUtils.MEMBER_SESSION_KEY).getClass().getName());
        logger.debug("member : {}님이 로그인하셨습니다.", member.getMemberId());

        return ResponseEntity.ok("로그인이 완료되었습니다!");
    }


    /**
     * 로그아웃 기능
     */
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        // 세션에서 DTO 가져오기
        MemberGetDto.Response memberDto = (MemberGetDto.Response) session.getAttribute(HttpSessionUtils.MEMBER_SESSION_KEY);
        if (memberDto != null) {
            logger.debug("member : {}님이 로그아웃하셨습니다.", memberDto.getMemberId());
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
        return ResponseEntity.ok("회원가입이 완료되었습니다!");
    }

    /**
     * 회원탈퇴 기능
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id, HttpSession session) {
        MemberGetDto.Response memberDto = (MemberGetDto.Response) session.getAttribute(HttpSessionUtils.MEMBER_SESSION_KEY);

        if (memberDto == null || !memberDto.getId().equals(id)) {
            throw new IllegalMemberAccessException("접근 권한이 없습니다.");
        }

        memberService.deleteMember(id);
        session.removeAttribute(HttpSessionUtils.MEMBER_SESSION_KEY); // 세션에서 해당 회원 정보 삭제
        logger.debug("member : {}님이 탈퇴하셨습니다.", memberDto.getMemberId());

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다!");
    }


    /**
     * 특정 회원의 프로필을 JSON 형태로 반환합니다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response> profile(@PathVariable Long id) {
        Response member = memberService.findMember(id);
        // DTO로 변환하여 반환
        return ResponseEntity.ok(Response.from(member));
    }

    /**
     * 회원 정보 수정 기능
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> updateData, HttpSession session) {
        Response member = memberService.findVerifiedMember(id, session);

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

        // DTO를 세션에 저장
        session.setAttribute(HttpSessionUtils.MEMBER_SESSION_KEY, Response.from(member));
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다!");
    }
}
