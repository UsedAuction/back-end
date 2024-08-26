package com.ddang.usedauction.point.service;

import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;

    // 포인트 잔액 조회
    public long getPointBalance(String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        return member.getPoint();
    }

    // 포인트 충전/사용 내역 조회
    public Page<PointHistory> getPointList(
        String email, LocalDate startDate, LocalDate endDate, String sorted, Pageable pageable
    ) {
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : LocalDate.of(2024, 1, 1).atStartOfDay();
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : LocalDateTime.now();;

        if (endDateTime.isBefore(startDateTime)) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        memberRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        Sort sort = sorting(sorted);
        Pageable sortPage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return pointRepository.findByMemberEmailAndCreatedAtBetween(email, startDateTime, endDateTime, sortPage);
    }

    // 정렬
    private Sort sorting(String sorted) {
        if ("latest".equals(sorted)) {
            return Sort.by(Direction.DESC, "createdAt");
        } else if ("oldest".equals(sorted)) {
            return Sort.by(Direction.ASC, "createdAt");
        } else {
            throw new IllegalArgumentException("잘못된 정렬값입니다.");
        }
    }
}
