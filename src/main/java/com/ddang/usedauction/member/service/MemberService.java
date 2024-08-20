package com.ddang.usedauction.member.service;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.dto.MemberGetDto;
import com.ddang.usedauction.member.dto.MemberServiceDto;
import com.ddang.usedauction.member.dto.MemberSignupDto;
import com.ddang.usedauction.member.dto.MemberUpdateDto;
import com.ddang.usedauction.member.exception.MemberNotFoundException;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.exception.MemberErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 하드코딩된 회원 정보 (임시)
    private static final String memberId = "testUser";
    private static final String passWord = "password123";
    private static final String email = "testUser@example.com";
    private static final String HARDCODED_TOKEN = "hardcoded-auth-token";

    // 메모리에 저장된 회원 정보 시뮬레이션 (임시)
    private final Map<String, String> signupUsers = new HashMap<>();
    private final Map<String, String> tokenStore = new HashMap<>();  // Token storage

    /**
     * 회원가입
     */
    public MemberServiceDto signup (MemberSignupDto.Request request) {

        if (memberRepository.existsByMemberIdAndDeleteDate(memberId, null)) { // 중복된 아이디인 경우
            throw new MemberException(MemberErrorCode.EXIST_USER_ID);
        }

        if (memberRepository.existsByEmailAndDeleteDate(email, null)) { // 중복된 이메일인 경우
            throw new MemberException(MemberErrorCode.EXIST_EMAIL);
        }

        Member member = Member.builder()
                .memberId(memberId)
                .passWord(passWord)
                .email(email)
                .build();
        Member save = memberRepository.save(member);

        return save.toServiceDto();
    }

    /**
     * 로그인
     */
    public String login(String memberId, String password) {
        if (!memberId.equals(memberId) || !passWord.equals(password)) {
            throw new MemberNotFoundException("잘못된 아이디 또는 비밀번호입니다.");
        }

        // 로그인 성공 시 하드코딩된 토큰 발급
        tokenStore.put(memberId, HARDCODED_TOKEN);
        return HARDCODED_TOKEN;
    }

    /**
     * 로그아웃
     */
    public void logout(String token) {
        // 하드코딩된 토큰 검증 후 로그아웃 처리
        if (HARDCODED_TOKEN.equals(token)) {
            tokenStore.remove(memberId);
        } else {
            throw new MemberNotFoundException("유효하지 않은 토큰입니다.");
        }
    }

    /**
     * 회원 정보 조회
     */
    public MemberGetDto.Response getProfile(Long id, String token) {
        if (!isValidToken(token)) {
            throw new MemberNotFoundException("유효하지 않은 토큰입니다.");
        }

        return new MemberGetDto.Response(memberId, email);
    }

    /**
     * 회원 정보 수정
     */
    public void updateMember(Long id, MemberUpdateDto.Request updateDto, String token) {
        if (!isValidToken(token)) {
            throw new MemberNotFoundException("유효하지 않은 토큰입니다.");
        }

        // 업데이트 로직 (임시로 하드코딩된 값을 수정했다고 가정)
        // 실제로는 데이터베이스에서 데이터를 업데이트해야 함
        if (updateDto.getMemberId() != null) {
            // 업데이트 시 회원 아이디를 변경할 수 있는 경우 (로직 추가)
        }
    }

    /**
     * 회원 탈퇴
     */
    public void deleteMember(Long id, String token) {
        if (!isValidToken(token)) {
            throw new MemberNotFoundException("유효하지 않은 토큰입니다.");
        }
        Member member = memberRepository.findByMemberIdAndDeleteDate(memberId, null)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        // 회원 정보 삭제 로직 (임시로 메모리에서 데이터 제거)
        memberRepository.delete(member);
        tokenStore.remove(passWord);
    }

    /**
     * 유효한 토큰인지 확인
     */
    private boolean isValidToken(String token) {
        return HARDCODED_TOKEN.equals(token) && tokenStore.containsKey(memberId);
    }
}
