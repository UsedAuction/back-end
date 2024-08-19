package com.ddang.usedauction.point.service;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;

    // 포인트 잔액 조회
    public long getPointBalance(UserDetails userDetails) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByEmail(username)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return member.getPoint();
    }

    // 포인트 충전/사용 내역 조회
    public Page<PointHistory> getPointList(
        UserDetails userDetails, LocalDate startDate, LocalDate endDate, Pageable pageable
    ) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        String username = userDetails.getUsername();
        memberRepository.findByEmail(username)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return pointRepository.findAllPoint(username, startDate, endDate, pageable);
    }
}
