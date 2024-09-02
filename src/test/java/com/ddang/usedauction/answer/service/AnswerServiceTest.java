package com.ddang.usedauction.answer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.answer.dto.AnswerCreateDto;
import com.ddang.usedauction.answer.dto.AnswerGetDto;
import com.ddang.usedauction.answer.dto.AnswerGetDto.Response;
import com.ddang.usedauction.answer.dto.AnswerUpdateDto;
import com.ddang.usedauction.answer.repository.AnswerRepository;
import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.repository.AskRepository;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.image.service.ImageService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.notification.service.NotificationService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private AskRepository askRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AnswerService answerService;

    Answer answer;

    @BeforeEach
    void setup() {

        Member seller = Member.builder()
            .memberId("seller")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("title")
            .seller(seller)
            .build();

        Image image = Image.builder()
            .imageUrl("url")
            .id(1L)
            .imageName("image")
            .imageType(ImageType.NORMAL)
            .build();

        answer = Answer.builder()
            .id(1L)
            .auction(auction)
            .imageList(List.of(image))
            .build();
    }

    @Test
    @DisplayName("답변 단건 조회")
    void getAnswer() {

        answer = answer.toBuilder()
            .title("title")
            .content("content")
            .build();

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        Response result = answerService.getAnswer(1L);

        assertEquals(1, result.getId());
        assertEquals("seller", result.getWriterId());
    }

    @Test
    @DisplayName("답변 단건 조회 실패 - 없는 답변")
    void getAnswerFail1() {

        when(answerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> answerService.getAnswer(1L));
    }

    @Test
    @DisplayName("회원이 작성한 답변 리스트 조회")
    void getAnswerList() {

        Member seller = Member.builder()
            .memberId("test")
            .email("test@naver.com")
            .build();

        Auction auction = Auction.builder()
            .seller(seller)
            .build();

        answer = answer.toBuilder()
            .auction(auction)
            .build();
        List<Answer> answerList = List.of(answer);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Answer> answerPageList = new PageImpl<>(answerList, pageable, answerList.size());

        when(answerRepository.findAllByMemberId("test", pageable)).thenReturn(
            answerPageList);

        Page<AnswerGetDto.Response> result = answerService.getAnswerList("test", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("test",
            result.getContent().get(0).getWriterId());
    }

    @Test
    @DisplayName("답변 생성")
    void createAnswer() {

        Member seller = Member.builder()
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .seller(seller)
            .build();

        Member writer = Member.builder()
            .id(1L)
            .build();

        Ask ask = Ask.builder()
            .id(1L)
            .writer(writer)
            .build();

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .content("content")
            .auctionId(1L)
            .title("title")
            .askId(1L)
            .build();

        answer = answer.toBuilder()
            .ask(ask)
            .auction(auction)
            .title("title")
            .content("content")
            .build();

        MockMultipartFile mockImage = new MockMultipartFile("이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> multipartFileList = List.of(mockImage);

        Image image = Image.builder()
            .answer(answer)
            .build();
        List<Image> imageList = List.of(image);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(askRepository.findById(1L)).thenReturn(Optional.of(ask));
        when(imageService.uploadImageList(multipartFileList)).thenReturn(imageList);
        when(answerRepository.save(argThat(arg -> arg.getTitle().equals("title")))).thenReturn(
            answer);

        AnswerGetDto.Response result = answerService.createAnswer(multipartFileList, createDto,
            "memberId");

        assertEquals("title", result.getTitle());
    }

    @Test
    @DisplayName("답변 생성 실패 - 없는 경매")
    void createAnswerFail1() {

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .content("content")
            .auctionId(1L)
            .title("title")
            .askId(1L)
            .build();

        MockMultipartFile mockImage = new MockMultipartFile("이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> multipartFileList = List.of(mockImage);

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> answerService.createAnswer(multipartFileList, createDto, "test@naver.com"));
    }

    @Test
    @DisplayName("답변 생성 실패 - 없는 질문")
    void createAnswerFail2() {

        Member seller = Member.builder()
            .email("test@naver.com")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .seller(seller)
            .build();

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .content("content")
            .auctionId(1L)
            .title("title")
            .askId(1L)
            .build();

        MockMultipartFile mockImage = new MockMultipartFile("이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> multipartFileList = List.of(mockImage);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(askRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> answerService.createAnswer(multipartFileList, createDto, "test@naver.com"));
    }

    @Test
    @DisplayName("답변 생성 실패 - 판매자가 아님")
    void createAnswerFail3() {

        Member seller = Member.builder()
            .email("tes@naver.com")
            .memberId("memberId")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .seller(seller)
            .build();

        Member writer = Member.builder()
            .memberId("writer")
            .build();

        Ask ask = Ask.builder()
            .id(1L)
            .writer(writer)
            .build();

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .content("content")
            .auctionId(1L)
            .title("title")
            .askId(1L)
            .build();

        MockMultipartFile mockImage = new MockMultipartFile("이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> multipartFileList = List.of(mockImage);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(askRepository.findById(1L)).thenReturn(Optional.of(ask));

        assertThrows(IllegalStateException.class,
            () -> answerService.createAnswer(multipartFileList, createDto, "memberI"));
    }

    @Test
    @DisplayName("답변 수정")
    void updateAnswer() {

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .content("content")
            .imageFileNameList(List.of("imageFileName"))
            .build();

        MockMultipartFile mockImage = new MockMultipartFile("이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> multipartFileList = List.of(mockImage);

        Member seller = Member.builder()
            .email("test@naver.com")
            .memberId("memberId")
            .build();

        Auction auction = Auction.builder()
            .seller(seller)
            .build();

        answer = answer.toBuilder()
            .auction(auction)
            .build();

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));
        when(answerRepository.save(argThat(arg -> arg.getId().equals(1L)))).thenReturn(answer);

        AnswerGetDto.Response result = answerService.updateAnswer(1L, multipartFileList, updateDto,
            "memberId");

        assertEquals(1, result.getId());
    }

    @Test
    @DisplayName("답변 수정 실패 - 없는 문의")
    void updateAnswerFail1() {

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .content("content")
            .imageFileNameList(List.of("imageFileName"))
            .build();

        MockMultipartFile mockImage = new MockMultipartFile("이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> multipartFileList = List.of(mockImage);

        when(answerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> answerService.updateAnswer(1L, multipartFileList, updateDto, "test@naver.com"));
    }

    @Test
    @DisplayName("답변 수정 실패 - 작성자가 다른 경우")
    void updateAnswerFail2() {

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .content("content")
            .imageFileNameList(List.of("imageFileName"))
            .build();

        MockMultipartFile mockImage = new MockMultipartFile("이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> multipartFileList = List.of(mockImage);

        Member seller = Member.builder()
            .memberId("memberI")
            .email("test@naver.com")
            .build();

        Auction auction = Auction.builder()
            .seller(seller)
            .build();

        answer = answer.toBuilder()
            .auction(auction)
            .build();

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        assertThrows(IllegalStateException.class,
            () -> answerService.updateAnswer(1L, multipartFileList, updateDto, "memberId"));
    }

    @Test
    @DisplayName("답변 삭제")
    void deleteAnswer() {

        Member seller = Member.builder()
            .email("test@naver.com")
            .memberId("test")
            .build();

        Auction auction = Auction.builder()
            .seller(seller)
            .build();

        answer = answer.toBuilder()
            .auction(auction)
            .build();

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        answerService.deleteAnswer("test", 1L);

        verify(answerRepository, times(1)).save(argThat(arg -> !arg.getDeletedAt().equals(null)));
    }

    @Test
    @DisplayName("답변 삭제 실패 - 없는 답변")
    void deleteAnswerFail1() {

        when(answerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> answerService.deleteAnswer("test", 1L));
    }
}