package com.ddang.usedauction.member.service;

import com.ddang.usedauction.member.component.MailComponent;
import com.ddang.usedauction.member.controller.HttpSessionUtils;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.dto.MemberGetDto.Response;
import com.ddang.usedauction.member.exception.DuplicateEmailFoundException;
import com.ddang.usedauction.member.exception.DuplicateMemberIdFoundException;
import com.ddang.usedauction.member.exception.IllegalMemberAccessException;
import com.ddang.usedauction.member.exception.MemberNotFoundException;
import com.ddang.usedauction.member.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MailComponent mailComponent;

    public MemberService(MemberRepository memberRepository, MailComponent mailComponent) {
        this.memberRepository = memberRepository;
        this.mailComponent = mailComponent;
    }

    public void create(Member member) {
        memberRepository.findByMemberId(member.getMemberId())
                .ifPresent(u -> { throw new DuplicateMemberIdFoundException(); });
        memberRepository.findByEmail(member.getEmail())
                .ifPresent(u -> { throw new DuplicateEmailFoundException(); });

        memberRepository.save(member);

        // 축하 이메일 발송
        sendWelcomeEmail(member);
    }

    private void sendWelcomeEmail(Member member) {
        String fromEmail = "seungh2206@gmail.com";
        String fromName = "관리자";
        String toEmail = member.getEmail();
        String toName = member.getMemberId();
        String title = "[땅땅땅!] 회원가입을 축하드립니다.";
        String contents = "땅땅땅! 중고물품 거래 웹사이트 회원가입을 축하드립니다.";
        mailComponent.send(fromEmail, fromName, toEmail, toName, title, contents);
    }

    public void deleteMember(Long id, HttpSession session) {
        checkPermission(id, session);
        memberRepository.deleteById(id);
        session.removeAttribute(HttpSessionUtils.MEMBER_SESSION_KEY);
    }

    public void updateMember(Long id, Map<String, String> updateData, HttpSession session) {
        Member member = findVerifiedMember(id, session);

        String password = updateData.get("password");
        if (!member.isValidPassWord(password)) {
            throw new IllegalMemberAccessException("비밀번호가 일치하지 않습니다.");
        }

        String newMemberId = updateData.get("memberId");
        if (newMemberId != null && !newMemberId.isEmpty()) {
            member.setMemberId(newMemberId);
        }

        member.setEmail(updateData.get("email"));
        memberRepository.save(member);
    }

    public Response getProfile(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
        return Response.from(member);
    }

    public void login(String memberId, String password, HttpSession session) {
        Member member = findByMemberId(memberId);
        if (!member.isValidPassWord(password)) {
            throw new IllegalMemberAccessException("비밀번호가 틀렸습니다.");
        }
        session.setAttribute(HttpSessionUtils.MEMBER_SESSION_KEY, Response.from(member));
    }

    public String logout(HttpSession session) {
        Response memberDto = (Response) session.getAttribute(HttpSessionUtils.MEMBER_SESSION_KEY);
        if (memberDto != null) {
            session.removeAttribute(HttpSessionUtils.MEMBER_SESSION_KEY);
            return memberDto.getMemberId();
        }
        return null;
    }

    private void checkPermission(Long id, HttpSession session) {
        if (!HttpSessionUtils.isLoginMember(session)) {
            throw new IllegalMemberAccessException("로그인이 필요합니다.");
        }

        Response sessionMemberDto = HttpSessionUtils.getSessionMember(session);
        if (sessionMemberDto == null || !sessionMemberDto.getId().equals(id)) {
            throw new IllegalMemberAccessException("접근 권한이 없습니다.");
        }
    }

    private Member findVerifiedMember(Long id, HttpSession session) {
        checkPermission(id, session);
        return memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
    }

    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
    }
}
