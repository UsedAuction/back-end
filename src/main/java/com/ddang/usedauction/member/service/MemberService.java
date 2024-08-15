package com.ddang.usedauction.member.service;

import com.ddang.usedauction.member.controller.HttpSessionUtils;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.exception.DuplicateEmailFoundException;
import com.ddang.usedauction.member.exception.DuplicateMemberIdFoundException;
import com.ddang.usedauction.member.exception.IllegalMemberAccessException;
import com.ddang.usedauction.member.exception.MemberNotFoundException;
import com.ddang.usedauction.member.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
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

    public List<Member> findMember() {
        return memberRepository.findAll();
    }

    public Member findMember(Long id) {
        return memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
    }

    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId).orElseThrow(MemberNotFoundException::new);
    }

    public Member findVerifiedMember(Long id, HttpSession session) {
        Member member = memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
        checkPermission(id, session);
        return member;
    }

    private void checkPermission(Long id, HttpSession session) {
        if (!HttpSessionUtils.isLoginMember(session)) {
            throw new IllegalMemberAccessException("로그인이 필요합니다.");
        }
        if (!findMember(id).equals(HttpSessionUtils.getSessionMember(session))) {
            throw new IllegalMemberAccessException();
        }
    }
}

