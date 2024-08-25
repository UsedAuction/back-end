package com.ddang.usedauction.point.controller;

import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.dto.PointBalanceDto;
import com.ddang.usedauction.point.dto.PointHistoryDto;
import com.ddang.usedauction.point.dto.PointHistoryDto.Response;
import com.ddang.usedauction.point.service.PointService;
import com.ddang.usedauction.security.auth.PrincipalDetails;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/points")
public class PointController {

    private final PointService pointService;

    // 포인트 잔액 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping()
    public ResponseEntity<PointBalanceDto.Response> getPointBalance(
        @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        String email = principalDetails.getName();
        long pointBalance = pointService.getPointBalance(email);

        return ResponseEntity.ok(PointBalanceDto.Response.from(pointBalance));
    }

    // 포인트 충전/사용 내역 조회
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/history")
    public ResponseEntity<Page<PointHistoryDto.Response>> getPointList(
        @AuthenticationPrincipal PrincipalDetails principalDetails,
        @RequestParam(value = "startDate") LocalDate startDate,
        @RequestParam(value = "endDate") LocalDate endDate,
        @PageableDefault Pageable pageable
    ) {
        String email = principalDetails.getName();
        Page<PointHistory> pointHistoryPage = pointService.getPointList(email, startDate, endDate, pageable);
        return ResponseEntity.ok(pointHistoryPage.map(Response::from));
    }
}
