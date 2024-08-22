package com.ddang.usedauction.answer.service;

import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.answer.dto.AnswerCreateDto;
import com.ddang.usedauction.answer.dto.AnswerUpdateDto;
import com.ddang.usedauction.answer.repository.AnswerRepository;
import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.repository.AskRepository;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.service.ImageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final AuctionRepository auctionRepository;
    private final AskRepository askRepository;
    private final ImageService imageService;

    /**
     * 답변 단건 조회
     *
     * @param answerId 답변 pk
     * @return 조회된 답변
     */
    @Transactional(readOnly = true)
    public Answer getAnswer(Long answerId) {

        return answerRepository.findById(answerId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 질문입니다."));
    }

    /**
     * 회원이 작성한 답변 리스트 조회
     *
     * @param memberEmail 회원 이메일
     * @param pageable    페이징
     * @return 페이징 처리된 답변 리스트
     */
    @Transactional(readOnly = true)
    public Page<Answer> getAnswerList(String memberEmail, Pageable pageable) {

        return answerRepository.findAllByMemberEmail(memberEmail, pageable);
    }

