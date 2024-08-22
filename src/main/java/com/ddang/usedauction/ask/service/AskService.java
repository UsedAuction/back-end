package com.ddang.usedauction.ask.service;

import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.dto.AskCreateDto;
import com.ddang.usedauction.ask.dto.AskUpdateDto;
import com.ddang.usedauction.ask.repository.AskRepository;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AskService {

    private final AskRepository askRepository;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;

    /**
     * 문의 단건 조회
     *
     * @param askId 문의 pk
     * @return 조회된 문의
     */
    @Transactional(readOnly = true)
    public Ask getAsk(Long askId) {

        return askRepository.findById(askId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 문의입니다."));
    }

    /**
     * 회원이 작성한 문의 리스트 조회
     *
     * @param memberEmail 회원 이메일
     * @param pageable    페이징
     * @return 페이징된 문의 리스트
     */
    @Transactional(readOnly = true)
    public Page<Ask> getAskList(String memberEmail, Pageable pageable) {

        return askRepository.findAllByMemberEmail(memberEmail, pageable);
    }

