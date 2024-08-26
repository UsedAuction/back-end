package com.ddang.usedauction.notification.service;

import static com.ddang.usedauction.auction.domain.AuctionState.CONTINUE;
import static com.ddang.usedauction.notification.domain.NotificationType.ANSWER;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.answer.dto.AnswerCreateDto;
import com.ddang.usedauction.answer.repository.AnswerRepository;
import com.ddang.usedauction.answer.service.AnswerService;
import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.repository.AskRepository;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.service.ImageService;
import com.ddang.usedauction.member.domain.Member;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class NotificationServiceAnswerTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AskRepository askRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private AnswerService answerService;

    private Member seller;
    private Member buyer;
    private Auction auction;
    private AnswerCreateDto createDto;
    private Ask ask;
    private Answer answer;
    private List<MultipartFile> multipartFileList;
    private List<Image> imageList;

    @BeforeEach
    void before() {

        seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .email("seller@exmaple.com")
            .build();

        buyer = Member.builder()
            .id(2L)
            .memberId("buyer")
            .build();

        auction = Auction.builder()
            .id(1L)
            .title("title")
            .auctionState(CONTINUE)
            .seller(seller)
            .build();

        ask = Ask.builder()
            .id(1L)
            .writer(buyer)
            .build();

        answer = Answer.builder()
            .id(1L)
            .title("답변 제목")
            .content("답변 내용")
            .auction(auction)
            .ask(ask)
            .build();

        createDto = AnswerCreateDto.builder()
            .title("답변 제목")
            .content("답변 내용")
            .auctionId(auction.getId())
            .askId(ask.getId())
            .build();

        MockMultipartFile mockImage = new MockMultipartFile(
            "이미지", "image.png", MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        multipartFileList = List.of(mockImage);

        Image image = Image.builder()
            .answer(answer)
            .build();
        imageList = List.of(image);
    }

    @Test
    @DisplayName("판매자가 답변작성시 질문자에게 알림 전송 - 성공")
    void ask_success() {

        //given
        given(auctionRepository.findById(createDto.getAuctionId())).willReturn(Optional.of(auction));
        given(askRepository.findById(createDto.getAskId())).willReturn(Optional.of(ask));
        given(imageService.uploadImageList(multipartFileList)).willReturn(imageList);
        given(answerRepository.save(
            argThat(arg -> arg.getTitle().equals(createDto.getTitle()) &&
                arg.getContent().equals(createDto.getContent()))
        )).willReturn(answer);

        //when
        answerService.createAnswer(multipartFileList, createDto, seller.getEmail());

        //then
        verify(notificationService, times(1))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getSeller().getMemberId() + "님이 " + auction.getTitle() + " 경매에 남긴 문의에 대한 답변을 달았습니다.",
                ANSWER
            );
    }

    @Test
    @DisplayName("판매자가 답변작성시 질문자에게 알림 전송 - 실패(존재하지 않는 경매)")
    void ask_fail_1() {

        //given
        given(auctionRepository.findById(createDto.getAuctionId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> answerService.createAnswer(multipartFileList, createDto, seller.getEmail()));

        //then
        verify(notificationService, times(0))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getSeller().getMemberId() + "님이 " + auction.getTitle() + " 경매에 남긴 문의에 대한 답변을 달았습니다.",
                ANSWER
            );
    }

    @Test
    @DisplayName("판매자가 답변작성시 질문자에게 알림 전송 - 실패(존재하지 않는 질문글)")
    void ask_fail_2() {

        //given
        given(auctionRepository.findById(createDto.getAuctionId())).willReturn(Optional.of(auction));
        given(askRepository.findById(createDto.getAskId())).willReturn(Optional.empty());

        //when
        assertThrows(NoSuchElementException.class,
            () -> answerService.createAnswer(multipartFileList, createDto, seller.getEmail()));

        //then
        verify(notificationService, times(0))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getSeller().getMemberId() + "님이 " + auction.getTitle() + " 경매에 남긴 문의에 대한 답변을 달았습니다.",
                ANSWER
            );
    }

    @Test
    @DisplayName("판매자가 답변작성시 질문자에게 알림 전송 - 실패(판매자가 아닌 경우)")
    void ask_fail_3() {

        //given
        seller = Member.builder()
            .id(1L)
            .memberId("seller")
            .email("noSeller@exmaple.com")
            .build();

        given(auctionRepository.findById(createDto.getAuctionId())).willReturn(Optional.of(auction));
        given(askRepository.findById(createDto.getAskId())).willReturn(Optional.of(ask));

        //when
        assertThrows(IllegalStateException.class,
            () -> answerService.createAnswer(multipartFileList, createDto, seller.getEmail()));

        //then
        verify(notificationService, times(0))
            .send(
                buyer.getId(),
                auction.getId(),
                auction.getSeller().getMemberId() + "님이 " + auction.getTitle() + " 경매에 남긴 문의에 대한 답변을 달았습니다.",
                ANSWER
            );
    }
}