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

    /**
     * 답변 생성 서비스
     *
     * @param imageList   이미지 리스트
     * @param createDto   답변 생성 정보
     * @param writerEmail 작성자 이메일
     * @return 작성된 답변
     */
    @Transactional
    public Answer createAnswer(List<MultipartFile> imageList, AnswerCreateDto createDto,
        String writerEmail) {

        Auction auction = auctionRepository.findById(createDto.getAuctionId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        Ask ask = askRepository.findById(createDto.getAskId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 질문글입니다."));

        if (!auction.getSeller().getEmail().equals(writerEmail)) { // 판매자가 아닌 경우
            throw new IllegalStateException("판매자만 답변을 작성할 수 있습니다.");
        }

        Answer answer = Answer.builder()
            .ask(ask)
            .auction(auction)
            .title(createDto.getTitle())
            .content(createDto.getContent())
            .build();

        saveImage(imageList, answer);

        return answerRepository.save(answer);
    }

    /**
     * 답변 수정
     *
     * @param answerId    수정할 답변 pk
     * @param imageList   추가할 이미지
     * @param updateDto   수정할 정보
     * @param memberEmail 회원 이메일
     * @return 수정된 답변
     */
    @Transactional
    public Answer updateAnswer(Long answerId, List<MultipartFile> imageList,
        AnswerUpdateDto updateDto, String memberEmail) {

        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 답글입니다."));

        // 작성자가 다른 경우
        if (!answer.getAuction().getSeller().getEmail().equals(memberEmail)) {
            throw new IllegalStateException("해당 문의글을 작성한 회원이 아닙니다.");
        }

        // 삭제하고자하는 이미지가 존재하는 경우
        if (updateDto.getImageFileNameList() != null && !updateDto.getImageFileNameList()
            .isEmpty()) {

            updateDto.getImageFileNameList().forEach(imageService::deleteImage);
        }

        answer = answer.toBuilder()
            .content(updateDto.getContent())
            .build();

        saveImage(imageList, answer);

        return answerRepository.save(answer);
    }

    /**
     * 회원이 작성한 답변 삭제
     *
     * @param memberEmail 회원 이메일
     */
    @Transactional
    public void deleteAnswer(String memberEmail) {

        Answer answer = answerRepository.findByMemberEmail(memberEmail)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 답변입니다."));

        answer = answer.toBuilder()
            .deletedAt(LocalDateTime.now())
            .build();

        answerRepository.save(answer);
    }

    // 이미지 저장하는 메소드
    private void saveImage(List<MultipartFile> imageList, Answer answer) {

        if (imageList != null && !imageList.isEmpty()) {
            List<Image> images = imageService.uploadImageList(imageList);

            images.stream()
                .map(i -> i.toBuilder()
                    .answer(answer)
                    .build())
                .forEach(answer::addImage); // 이미지 리스트 저장
        }
    }
}
