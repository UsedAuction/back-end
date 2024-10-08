package com.ddang.usedauction.answer.service;

import static com.ddang.usedauction.notification.domain.NotificationType.ANSWER;

import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.answer.dto.AnswerCreateDto;
import com.ddang.usedauction.answer.dto.AnswerGetDto;
import com.ddang.usedauction.answer.dto.AnswerUpdateDto;
import com.ddang.usedauction.answer.repository.AnswerRepository;
import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.repository.AskRepository;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.service.ImageService;
import com.ddang.usedauction.notification.service.NotificationService;
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
    private final NotificationService notificationService;

    /**
     * 답변 단건 조회
     *
     * @param answerId 답변 pk
     * @return 조회된 답변
     */
    @Transactional(readOnly = true)
    public AnswerGetDto.Response getAnswer(Long answerId) {

        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 질문입니다."));

        return AnswerGetDto.Response.from(answer);
    }

    /**
     * 회원이 작성한 답변 리스트 조회
     *
     * @param memberId 회원 아이디
     * @param pageable 페이징
     * @return 페이징 처리된 답변 리스트
     */
    @Transactional(readOnly = true)
    public Page<AnswerGetDto.Response> getAnswerList(String memberId, Pageable pageable) {

        Page<Answer> answerPageList = answerRepository.findAllByMemberId(memberId, pageable);

        return answerPageList.map(AnswerGetDto.Response::from);
    }

    /**
     * 답변 생성 서비스
     *
     * @param imageList 이미지 리스트
     * @param createDto 답변 생성 정보
     * @param writerId  작성자 아이디
     * @return 작성된 답변
     */
    @Transactional
    public AnswerGetDto.Response createAnswer(List<MultipartFile> imageList,
        AnswerCreateDto createDto,
        String writerId) {

        Auction auction = auctionRepository.findById(createDto.getAuctionId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        Ask ask = askRepository.findById(createDto.getAskId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 질문글입니다."));

        if (!auction.getSeller().getMemberId().equals(writerId)) { // 판매자가 아닌 경우
            throw new IllegalStateException("판매자만 답변을 작성할 수 있습니다.");
        }

        Answer answer = Answer.builder()
            .ask(ask)
            .auction(auction)
            .title(createDto.getTitle())
            .content(createDto.getContent())
            .build();

        saveImage(imageList, answer);

        sendNotificationForAnswer(auction, ask.getWriter().getId());

        Answer savedAnswer = answerRepository.save(answer);

        return AnswerGetDto.Response.from(savedAnswer);
    }

    /**
     * 답변 수정
     *
     * @param answerId  수정할 답변 pk
     * @param imageList 추가할 이미지
     * @param updateDto 수정할 정보
     * @param memberId  회원 아이디
     * @return 수정된 답변
     */
    @Transactional
    public AnswerGetDto.Response updateAnswer(Long answerId, List<MultipartFile> imageList,
        AnswerUpdateDto updateDto, String memberId) {

        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 답글입니다."));

        // 작성자가 다른 경우
        if (!answer.getAuction().getSeller().getMemberId().equals(memberId)) {
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

        Answer savedAnswer = answerRepository.save(answer);

        return AnswerGetDto.Response.from(savedAnswer);
    }

    /**
     * 회원이 작성한 답변 삭제
     *
     * @param memberId 회원 아이디
     * @param answerId 삭제할 답변 pk
     */
    @Transactional
    public void deleteAnswer(String memberId, Long answerId) {

        Answer answer = answerRepository.findById(answerId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 답변입니다."));

        // 작성한 회원이 아닌 경우
        if (!answer.getAuction().getSeller().getMemberId().equals(memberId)) {
            throw new IllegalStateException("답변을 작성한 회원만 삭제 가능합니다.");
        }

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

    // 질문 작성자(입찰자)에게 알림 전송
    private void sendNotificationForAnswer(Auction auction, Long buyerId) {

        notificationService.send(
            buyerId,
            auction.getId(),
            auction.getSeller().getMemberId() + "님이 " + auction.getTitle()
                + " 경매에 남긴 문의에 대한 답변을 달았습니다.",
            ANSWER
        );
    }
}
