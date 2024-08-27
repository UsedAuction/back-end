package com.ddang.usedauction.bid.service;

import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.bid.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;

    /**
     * 회원의 입찰 목록 조회
     *
     * @param memberId 회원 아이디
     * @param pageable 페이징
     * @return 페이징 처리된 입찰 목록
     */
    @Transactional(readOnly = true)
    public Page<Bid> getBidList(String memberId, Pageable pageable) {

        return bidRepository.findAllByMemberId(memberId, pageable);
    }
}
