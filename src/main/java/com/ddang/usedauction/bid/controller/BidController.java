package com.ddang.usedauction.bid.controller;

import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.bid.dto.BidGetDto;
import com.ddang.usedauction.bid.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    /**
     * 회원의 입찰 목록 조회 컨트롤러
     *
     * @param pageable 페이징
     * @return 성공 시 200 코드와 입찰 목록 리스트, 실패 시 에러코드와 에러메시지
     */
    @GetMapping
    public ResponseEntity<Page<BidGetDto.Response>> getBidListController(
        @PageableDefault Pageable pageable) {

        String memberId = "test"; // todo: 토큰을 이용한 조회

        Page<Bid> bidList = bidService.getBidList(memberId, pageable);

        return ResponseEntity.ok(bidList.map(BidGetDto.Response::from));
    }
}
