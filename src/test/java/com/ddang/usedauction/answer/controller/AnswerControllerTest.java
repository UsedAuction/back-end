package com.ddang.usedauction.answer.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.answer.dto.AnswerCreateDto;
import com.ddang.usedauction.answer.dto.AnswerGetDto;
import com.ddang.usedauction.answer.dto.AnswerUpdateDto;
import com.ddang.usedauction.answer.service.AnswerService;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.config.SecurityConfig;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.security.auth.PrincipalOauth2UserService;
import com.ddang.usedauction.security.jwt.Oauth2FailureHandler;
import com.ddang.usedauction.security.jwt.Oauth2SuccessHandler;
import com.ddang.usedauction.security.jwt.TokenProvider;
import com.ddang.usedauction.token.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AnswerController.class, SecurityConfig.class})
class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrincipalOauth2UserService principalOauth2UserService;

    @MockBean
    private Oauth2SuccessHandler oauth2SuccessHandler;

    @MockBean
    private Oauth2FailureHandler oauth2FailureHandler;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private AnswerService answerService;

    Answer answer;

    @BeforeEach
    void setup() {

        Member seller = Member.builder()
            .memberId("test")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("auctionTitle")
            .seller(seller)
            .build();

        Image image = Image.builder()
            .answer(answer)
            .id(1L)
            .imageUrl("url")
            .imageType(ImageType.NORMAL)
            .imageName("name")
            .build();
        List<Image> imageList = List.of(image);

        answer = Answer.builder()
            .id(1L)
            .auction(auction)
            .title("answerTitle")
            .content("content")
            .imageList(imageList)
            .build();
    }

    @Test
    @DisplayName("답변 단건 조회 컨트롤러")
    void getAnswerController() throws Exception {

        when(answerService.getAnswer(1L)).thenReturn(AnswerGetDto.Response.from(answer));

        mockMvc.perform(get("/api/answers/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.writerId").value("test"));
    }

    @Test
    @DisplayName("답변 단건 조회 컨트롤러 실패 - url 경로 다름")
    void getAnswerControllerFail1() throws Exception {

        mockMvc.perform(get("/api/answer/1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("답변 단건 조회 컨트롤러 실패 - 유효성 검증 실패")
    void getAnswerControllerFail2() throws Exception {

        mockMvc.perform(get("/api/answers/0"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 작성한 답변 리스트 조회 컨트롤러")
    void getAnswerListController() throws Exception {

        Pageable pageable = PageRequest.of(0, 10, Direction.DESC, "createdAt");
        List<AnswerGetDto.Response> answerList = List.of(AnswerGetDto.Response.from(answer));
        Page<AnswerGetDto.Response> answerPageList = new PageImpl<>(answerList, pageable,
            answerList.size());

        when(answerService.getAnswerList("memberId", pageable)).thenReturn(answerPageList);

        mockMvc.perform(get("/api/answers"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @DisplayName("회원이 작성한 답변 리스트 조회 컨트롤러 실패 - 로그인 x")
    void getAnswerListControllerFail1() throws Exception {

        mockMvc.perform(get("/api/answers"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 작성한 답변 리스트 조회 컨트롤러 실패 - url 경로 다름")
    void getAnswerListControllerFail2() throws Exception {

        mockMvc.perform(get("/api/answer"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("답변 생성 컨트롤러")
    void createAnswerController() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .askId(1L)
            .auctionId(1L)
            .title("title")
            .content("content")
            .build();

        when(answerService.createAnswer(argThat(arg -> arg.get(0).getName().equals("imageList")),
            argThat(arg -> arg.getTitle().equals("title")),
            argThat(arg -> arg.equals("memberId")))).thenReturn(
            AnswerGetDto.Response.from(answer));

        mockMvc.perform(multipart("/api/answers")
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes(
                        StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("답변 생성 컨트롤러 실패 - 로그인 x")
    void createAnswerControllerFail1() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .askId(1L)
            .auctionId(1L)
            .title("title")
            .content("content")
            .build();

        mockMvc.perform(multipart("/api/answers")
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes(
                        StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("답변 생성 컨트롤러 실패 - url 경로 다름")
    void createAnswerControllerFail2() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .askId(1L)
            .auctionId(1L)
            .title("title")
            .content("content")
            .build();

        mockMvc.perform(multipart("/api/answer")
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes(
                        StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("답변 생성 컨트롤러 실패 - 이미지 유효성 검사 실패")
    void createAnswerControllerFail3() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.txt",
            MediaType.TEXT_PLAIN_VALUE, "image".getBytes());

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .askId(1L)
            .auctionId(1L)
            .title("title")
            .content("content")
            .build();

        mockMvc.perform(multipart("/api/answers")
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes(
                        StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("답변 생성 컨트롤러 실패 - dto 유효성 검사 실패")
    void createAnswerControllerFail4() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.jpeg",
            MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        AnswerCreateDto createDto = AnswerCreateDto.builder()
            .build();

        mockMvc.perform(multipart("/api/answers")
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes(
                        StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("답변 수정 컨트롤러")
    void updateAnswerController() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.jpeg",
            MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .imageFileNameList(List.of("name"))
            .content("content")
            .build();

        when(answerService.updateAnswer(argThat(arg -> arg.equals(1L)),
            argThat(arg -> arg.get(0).getName().equals("imageList")),
            argThat(arg -> arg.getContent().equals("content")),
            argThat(arg -> arg.equals("memberId")))).thenReturn(AnswerGetDto.Response.from(answer));

        mockMvc.perform(multipart("/api/answers/1")
                .file(mockImage)
                .file(new MockMultipartFile("updateDto", "", "application/json",
                    objectMapper.writeValueAsString(updateDto).getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("content"));
    }

    @Test
    @DisplayName("답변 수정 컨트롤러 실패 - 로그인 x")
    void updateAnswerControllerFail1() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.jpeg",
            MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .imageFileNameList(List.of("name"))
            .content("content")
            .build();

        mockMvc.perform(multipart("/api/answers/1")
                .file(mockImage)
                .file(new MockMultipartFile("updateDto", "", "application/json",
                    objectMapper.writeValueAsString(updateDto).getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("답변 수정 컨트롤러 실패 - url 경로 다름")
    void updateAnswerControllerFail2() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.jpeg",
            MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .imageFileNameList(List.of("name"))
            .content("content")
            .build();

        mockMvc.perform(multipart("/api/answer/1")
                .file(mockImage)
                .file(new MockMultipartFile("updateDto", "", "application/json",
                    objectMapper.writeValueAsString(updateDto).getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("답변 수정 컨트롤러 실패 - pathVariable 유효성 검사 실패")
    void updateAnswerControllerFail3() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.jpeg",
            MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .imageFileNameList(List.of("name"))
            .content("content")
            .build();

        mockMvc.perform(multipart("/api/answers/0")
                .file(mockImage)
                .file(new MockMultipartFile("updateDto", "", "application/json",
                    objectMapper.writeValueAsString(updateDto).getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("답변 수정 컨트롤러 실패 - 이미지 유효성 검사 실패")
    void updateAnswerControllerFail4() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.txt",
            MediaType.TEXT_PLAIN_VALUE, "image".getBytes());

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .imageFileNameList(List.of("name"))
            .content("content")
            .build();

        mockMvc.perform(multipart("/api/answers/1")
                .file(mockImage)
                .file(new MockMultipartFile("updateDto", "", "application/json",
                    objectMapper.writeValueAsString(updateDto).getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("답변 수정 컨트롤러 실패 - dto 유효성 검사 실패")
    void updateAnswerControllerFail5() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.jpeg",
            MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        AnswerUpdateDto updateDto = AnswerUpdateDto.builder()
            .build();

        mockMvc.perform(multipart("/api/answers/1")
                .file(mockImage)
                .file(new MockMultipartFile("updateDto", "", "application/json",
                    objectMapper.writeValueAsString(updateDto).getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 작성한 답변 삭제 컨트롤러")
    void deleteAnswerController() throws Exception {

        mockMvc.perform(delete("/api/answers/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("삭제되었습니다."));

        verify(answerService, times(1)).deleteAnswer("memberId", 1L);
    }

    @Test
    @DisplayName("회원이 작성한 답변 삭제 컨트롤러 실패 - 로그인 x")
    void deleteAnswerControllerFail1() throws Exception {

        mockMvc.perform(delete("/api/answers/1"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 작성한 답변 삭제 컨트롤러 실패 - url 경로 다름")
    void deleteAnswerControllerFail2() throws Exception {

        mockMvc.perform(delete("/api/answer"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}