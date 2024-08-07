package com.ddang.usedauction.point.service;

import static com.ddang.usedauction.member.MemberErrorCode.MEMBER_NOT_FOUND;
import static com.ddang.usedauction.point.exception.PointErrorCode.INVALID_DATE_RANGE;

import com.ddang.usedauction.member.Member;
import com.ddang.usedauction.member.MemberException;
import com.ddang.usedauction.member.MemberRepository;
import com.ddang.usedauction.point.dto.PointBalanceDto;
import com.ddang.usedauction.point.dto.PointHistoryDto;
import com.ddang.usedauction.point.exception.PointException;
import com.ddang.usedauction.point.repository.PointRepository;
import java.time.LocalDate;
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
    public PointBalanceDto getPointBalance(UserDetails userDetails) {
        String username = userDetails.getUsername();
        Member member = memberRepository.findByEmail(username)
            .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

        return new PointBalanceDto(member.getPoint());
    }

    // 포인트 충전/사용 내역 조회
    public Page<PointHistoryDto> getPointList(
        UserDetails userDetails, LocalDate startDate, LocalDate endDate, Pageable pageable
    ) {
        if (endDate.isBefore(startDate)) {
            throw new PointException(INVALID_DATE_RANGE);
        }

        String username = userDetails.getUsername();
        memberRepository.findByEmail(username)
            .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

        return pointRepository.findAllPoint(username, startDate, endDate, pageable)
            .map(PointHistoryDto::fromPointHistory);
    }
}
