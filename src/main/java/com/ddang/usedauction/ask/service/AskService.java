package com.ddang.usedauction.ask.service;

import static com.ddang.usedauction.notification.domain.NotificationType.QUESTION;

import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.dto.AskCreateDto;
import com.ddang.usedauction.ask.dto.AskGetDto;
import com.ddang.usedauction.ask.dto.AskUpdateDto;
import com.ddang.usedauction.ask.repository.AskRepository;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.service.NotificationService;
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
    private final NotificationService notificationService;

    /**
     * 문의 단건 조회
     *
     * @param askId 문의 pk
     * @return 조회된 문의
     */
    @Transactional(readOnly = true)
    public AskGetDto.Response getAsk(Long askId) {

        Ask ask = askRepository.findById(askId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 문의입니다."));

        return AskGetDto.Response.from(ask);
    }

    /**
     * 회원이 작성한 문의 리스트 조회
     *
     * @param memberId 회원 아이디
     * @param pageable 페이징
     * @return 페이징된 문의 리스트
     */
    @Transactional(readOnly = true)
    public Page<AskGetDto.Response> getAskList(String memberId, Pageable pageable) {

        Page<Ask> askPageList = askRepository.findAllByMemberId(memberId, pageable);

        return askPageList.map(AskGetDto.Response::from);
    }

    /**
     * 회원이 받은 문의 리스트 조회
     *
     * @param memberId 회원 아이디
     * @param pageable 페이징
     * @return 페이징된 문의 리스트
     */
    @Transactional(readOnly = true)
    public Page<AskGetDto.Response> getReceiveAskList(String memberId, Pageable pageable) {

        Page<Ask> askPageList = askRepository.findALlBySellerId(memberId, pageable);

        return askPageList.map(AskGetDto.Response::from);
    }

    /**
     * 문의 생성
     *
     * @param createDto 문의 정보
     * @param memberId  작성자 아이디
     * @return 생성된 문의
     */
    @Transactional
    public AskGetDto.Response createAsk(AskCreateDto createDto, String memberId) {

        Auction auction = auctionRepository.findById(createDto.getAuctionId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        // 이미 경매가 종료된 경우
        if (auction.getAuctionState().equals(AuctionState.END)) {
            throw new IllegalStateException("종료된 경매에는 문의글을 남길 수 없습니다.");
        }

        Ask ask = Ask.builder()
            .title(createDto.getTitle())
            .writer(member)
            .content(createDto.getContent())
            .auction(auction)
            .build();

        sendNotificationForQuestion(auction, member.getMemberId());

        Ask savedAsk = askRepository.save(ask);

        return AskGetDto.Response.from(savedAsk);
    }

    /**
     * 문의 수정
     *
     * @param askId     수정할 문의 pk
     * @param updateDto 수정 정보
     * @param memberId  회원 아이디
     * @return 수정된 문의
     */
    @Transactional
    public AskGetDto.Response updateAsk(Long askId, AskUpdateDto updateDto, String memberId) {

        Ask ask = askRepository.findById(askId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 문의입니다."));

        // 작성자가 다른 경우
        if (!ask.getWriter().getMemberId().equals(memberId)) {
            throw new IllegalStateException("문의글을 작성한 본인만 수정할 수 있습니다.");
        }

        ask = ask.toBuilder()
            .content(updateDto.getContent())
            .build();

        Ask savedAsk = askRepository.save(ask);

        return AskGetDto.Response.from(savedAsk);
    }

    /**
     * 회원이 작성한 문의 삭제
     *
     * @param memberId 회원 아이디
     * @param askId    삭제할 문의 pk
     */
    @Transactional
    public void deleteAsk(String memberId, Long askId) {

        Ask ask = askRepository.findById(askId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 문의입니다."));

        // 작성한 회원이 아닌 경우
        if (!ask.getWriter().getMemberId().equals(memberId)) {
            throw new IllegalStateException("작성한 회원만 삭제할 수 있습니다.");
        }

        ask = ask.toBuilder()
            .deletedAt(LocalDateTime.now())
            .build();

        askRepository.save(ask);
    }

    // 판매자에게 알림 전송
    private void sendNotificationForQuestion(Auction auction, String memberId) {

        notificationService.send(
            auction.getSeller().getId(),
            auction.getId(),
            memberId + "님이 " + auction.getTitle() + " 경매에 문의를 남겼습니다.",
            QUESTION
        );
    }
}
