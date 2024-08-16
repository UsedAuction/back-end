package com.ddang.usedauction.member.service;

import com.ddang.usedauction.member.component.MailComponent;
import com.ddang.usedauction.member.controller.HttpSessionUtils;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.dto.MemberGetDto;
import com.ddang.usedauction.member.dto.MemberGetDto.Response;
import com.ddang.usedauction.member.exception.DuplicateEmailFoundException;
import com.ddang.usedauction.member.exception.DuplicateMemberIdFoundException;
import com.ddang.usedauction.member.exception.IllegalMemberAccessException;
import com.ddang.usedauction.member.exception.MemberNotFoundException;
import com.ddang.usedauction.member.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MailComponent mailComponent;

    @Autowired
    public MemberService(MemberRepository memberRepository, JavaMailSender javaMailSender, MailComponent mailComponent) {
        this.memberRepository = memberRepository;
        this.mailComponent = mailComponent;
    }

    public void create(Member member) {
        memberRepository.findByMemberId(member.getMemberId())
                .ifPresent(u -> {
                    throw new DuplicateMemberIdFoundException();
                });
        memberRepository.findByEmail(member.getEmail())
                .ifPresent(u -> {
                    throw new DuplicateEmailFoundException();
                });

        memberRepository.save(member);

        // 메일 전송
        String fromEmail = "seungh22@gmail.com";
        String fromName = "관리자";
        String toEmail = member.getEmail();
        String toName = member.getMemberId();

        String title = "[땅땅땅!] 회원가입을 축하드립니다.";
        String contents = "땅땅땅! 중고물품 거래 웹사이트 회원가입을 축하드립니다.";

        mailComponent.send(fromEmail, fromName, toEmail, toName, title, contents);
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    public void update(Member member) {
        memberRepository.save(member);
    }

    public void update(Member member, Member updatedMember) {
        member.update(updatedMember);
        memberRepository.save(member);
    }

    // 모든 회원을 조회하고 DTO로 반환
    public List<Response> findMembers() {
        return memberRepository.findAll().stream()
                .map(MemberGetDto.Response::from)
                .collect(Collectors.toList());
    }

    // 특정 회원을 조회하고 DTO로 반환
    public Response findMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
        return Response.from(member);
    }

    // 특정 memberId를 가진 회원을 조회하고 DTO로 반환
    public Response findByMemberId(String memberId) {
        Member member = memberRepository.findByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
        return Response.from(member);
    }

    // 특정 회원을 세션과 비교하여 검증 후 DTO로 반환
    public Response findVerifiedMember(Long id, HttpSession session) {
        Member member = memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
        checkPermission(id, session);
        return Response.from(member);
    }

    private void checkPermission(Long id, HttpSession session) {
        if (!HttpSessionUtils.isLoginMember(session)) {
            throw new IllegalMemberAccessException("로그인이 필요합니다.");
        }

        MemberGetDto.Response sessionMemberDto = (MemberGetDto.Response) session.getAttribute(HttpSessionUtils.MEMBER_SESSION_KEY);

        if (sessionMemberDto == null || !sessionMemberDto.getId().equals(id)) {
            throw new IllegalMemberAccessException("잘못된 접근입니다.");
        }
    }
}
