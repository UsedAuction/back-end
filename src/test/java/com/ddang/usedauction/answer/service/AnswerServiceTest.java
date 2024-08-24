package com.ddang.usedauction.answer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private AnswerService answerService;

    Answer answer;

    @BeforeEach
    void setup() {

        answer = Answer.builder()
            .id(1L)
            .build();
    }

    @Test
    @DisplayName("답변 단건 조회")
    void getAnswer() {

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));

        Answer result = answerService.getAnswer(1L);

        assertEquals(1, result.getId());
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

        when(answerRepository.findAllByMemberEmail("test@naver.com", pageable)).thenReturn(
            answerPageList);

        Page<Answer> result = answerService.getAnswerList("test@naver.com", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("test@naver.com",
            result.getContent().get(0).getAuction().getSeller().getEmail());
    }

    @Test
    @DisplayName("답변 생성")
    void createAnswer() {

        Member seller = Member.builder()
            .email("test@naver.com")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .seller(seller)
            .build();

        Ask ask = Ask.builder()
            .id(1L)
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

        Answer result = answerService.createAnswer(multipartFileList, createDto, "test@naver.com");

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
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .seller(seller)
            .build();

        Ask ask = Ask.builder()
            .id(1L)
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
            () -> answerService.createAnswer(multipartFileList, createDto, "test@naver.com"));
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
            .build();

        Auction auction = Auction.builder()
            .seller(seller)
            .build();

        answer = answer.toBuilder()
            .auction(auction)
            .build();

        when(answerRepository.findById(1L)).thenReturn(Optional.of(answer));
        when(answerRepository.save(argThat(arg -> arg.getId().equals(1L)))).thenReturn(answer);

        Answer result = answerService.updateAnswer(1L, multipartFileList, updateDto,
            "test@naver.com");

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
            () -> answerService.updateAnswer(1L, multipartFileList, updateDto, "tes@naver.com"));
    }

    @Test
    @DisplayName("답변 삭제")
    void deleteAnswer() {

        Member seller = Member.builder()
            .email("test@naver.com")
            .build();

        Auction auction = Auction.builder()
            .seller(seller)
            .build();

        answer = answer.toBuilder()
            .auction(auction)
            .build();

        when(answerRepository.findByMemberEmail("test@naver.com")).thenReturn(Optional.of(answer));

        answerService.deleteAnswer("test@naver.com");

        verify(answerRepository, times(1)).save(argThat(arg -> !arg.getDeletedAt().equals(null)));
    }

    @Test
    @DisplayName("답변 삭제 실패 - 없는 답변")
    void deleteAnswerFail1() {

        when(answerRepository.findByMemberEmail("test@naver.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> answerService.deleteAnswer("test@naver.com"));
    }
}