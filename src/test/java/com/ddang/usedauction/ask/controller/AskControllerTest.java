package com.ddang.usedauction.ask.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.answer.domain.Answer;
import com.ddang.usedauction.ask.domain.Ask;
import com.ddang.usedauction.ask.dto.AskCreateDto;
import com.ddang.usedauction.ask.dto.AskUpdateDto;
import com.ddang.usedauction.ask.service.AskService;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({AskController.class, SecurityConfig.class})
class AskControllerTest {

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
    private AskService askService;

    Ask ask;

    @BeforeEach
    void setup() {

        Member seller = Member.builder()
            .memberId("seller")
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("auctionTitle")
            .seller(seller)
            .build();

        Member writer = Member.builder()
            .memberId("test")
            .build();

        Answer answer = Answer.builder()
            .id(1L)
            .auction(auction)
            .title("answerTitle")
            .content("content")
            .auction(auction)
            .build();

        Image image = Image.builder()
            .answer(answer)
            .id(1L)
            .imageUrl("url")
            .imageType(ImageType.NORMAL)
            .imageName("name")
            .build();
        List<Image> imageList = List.of(image);

        answer = answer.toBuilder()
            .imageList(imageList)
            .build();

        ask = Ask.builder()
            .id(1L)
            .auction(auction)
            .title("title")
            .content("content")
            .writer(writer)
            .answerList(List.of(answer))
            .build();
    }

    @Test
    @DisplayName("문의 단건 조회 컨트롤러")
    void getAskController() throws Exception {

        when(askService.getAsk(1L)).thenReturn(ask);

        mockMvc.perform(get("/api/asks/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("title"))
            .andExpect(jsonPath("$.answerList[0].title").value("answerTitle"));
    }

    @Test
    @DisplayName("문의 단건 조회 컨트롤러 실패 - url 경로 다름")
    void getAskControllerFail1() throws Exception {

        when(askService.getAsk(1L)).thenReturn(ask);

        mockMvc.perform(get("/api/ask/1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("문의 단건 조회 컨트롤러 실패 - pathVariable 유효성 검사 실패")
    void getAskControllerFail2() throws Exception {

        when(askService.getAsk(1L)).thenReturn(ask);

        mockMvc.perform(get("/api/asks/0"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 작성한 문의 리스트 조회 컨트롤러")
    void getAskList() throws Exception {

        Pageable pageable = PageRequest.of(0, 10, Direction.DESC, "createdAt");

        List<Ask> askList = List.of(ask);
        Page<Ask> askPageList = new PageImpl<>(askList, pageable, askList.size());

        when(askService.getAskList("memberId", pageable)).thenReturn(askPageList);

        mockMvc.perform(get("/api/asks"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].title").value("title"));
    }

    @Test
    @DisplayName("회원이 작성한 문의 리스트 조회 컨트롤러 실패 - 로그인 x")
    void getAskListFail1() throws Exception {

        mockMvc.perform(get("/api/asks"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 작성한 문의 리스트 조회 컨트롤러 실패 - url 경로 다름")
    void getAskListFail2() throws Exception {

        mockMvc.perform(get("/api/ask"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 받은 문의 리스트 조회 컨트롤러")
    void getReceiveAskListController() throws Exception {

        Member seller = Member.builder()
            .memberId("memberId")
            .build();

        Member writer = Member.builder()
            .memberId("writer")
            .build();

        Auction auction = Auction.builder()
            .seller(seller)
            .title("auctionTitle")
            .build();

        ask = ask.toBuilder()
            .auction(auction)
            .writer(writer)
            .build();

        Pageable pageable = PageRequest.of(0, 10, Direction.DESC, "createdAt");
        List<Ask> askList = List.of(ask);
        Page<Ask> askPageList = new PageImpl<>(askList, pageable, askList.size());

        when(askService.getReceiveAskList("memberId", pageable)).thenReturn(askPageList);

        mockMvc.perform(get("/api/asks/receive"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].auctionTitle").value("auctionTitle"))
            .andExpect(jsonPath("$.content[0].writerId").value("writer"));
    }

    @Test
    @DisplayName("회원이 받은 문의 리스트 조회 컨트롤러 실패 - 로그인 x")
    void getReceiveAskListControllerFail1() throws Exception {

        mockMvc.perform(get("/api/asks/receive"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원이 받은 문의 리스트 조회 컨트롤러 실패 - url 경로 다름")
    void getReceiveAskListControllerFail2() throws Exception {

        mockMvc.perform(get("/api/ask/receive"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("문의 생성 컨트롤러")
    void createAskController() throws Exception {

        AskCreateDto createDto = AskCreateDto.builder()
            .content("content")
            .auctionId(1L)
            .title("title")
            .build();

        when(askService.createAsk(argThat(arg -> arg.getTitle().equals("title")),
            argThat(arg -> arg.equals("memberId")))).thenReturn(ask);

        mockMvc.perform(post("/api/asks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("title"));
    }

    @Test
    @DisplayName("문의 생성 컨트롤러 실패 - 로그인 x")
    void createAskControllerFail1() throws Exception {

        AskCreateDto createDto = AskCreateDto.builder()
            .content("content")
            .auctionId(1L)
            .title("title")
            .build();

        mockMvc.perform(post("/api/asks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("문의 생성 컨트롤러 실패 - url 경로 다름")
    void createAskControllerFail2() throws Exception {

        AskCreateDto createDto = AskCreateDto.builder()
            .content("content")
            .auctionId(1L)
            .title("title")
            .build();

        mockMvc.perform(post("/api/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("문의 생성 컨트롤러 실패 - dto 유효성 검증 실패")
    void createAskControllerFail3() throws Exception {

        AskCreateDto createDto = AskCreateDto.builder()
            .auctionId(-1L)
            .build();

        mockMvc.perform(post("/api/asks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("문의 수정 컨트롤러")
    void updateAskController() throws Exception {

        AskUpdateDto updateDto = AskUpdateDto.builder()
            .content("content1")
            .build();

        when(askService.updateAsk(argThat(arg -> arg.equals(1L)),
            argThat(arg -> arg.getContent().equals("content1")),
            argThat(arg -> arg.equals("memberId")))).thenReturn(
            ask.toBuilder().content("content1").build());

        mockMvc.perform(put("/api/asks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("content1"));
    }

    @Test
    @DisplayName("문의 수정 컨트롤러 실패 - 로그인 x")
    void updateAskControllerFail1() throws Exception {

        AskUpdateDto updateDto = AskUpdateDto.builder()
            .content("content1")
            .build();

        mockMvc.perform(put("/api/asks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("문의 수정 컨트롤러 실패 - url 경로 다름")
    void updateAskControllerFail2() throws Exception {

        AskUpdateDto updateDto = AskUpdateDto.builder()
            .content("content1")
            .build();

        mockMvc.perform(put("/api/ask/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("문의 수정 컨트롤러 실패 - pathVariable 유효성 검사 실패")
    void updateAskControllerFail3() throws Exception {

        AskUpdateDto updateDto = AskUpdateDto.builder()
            .content("content1")
            .build();

        mockMvc.perform(put("/api/asks/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("문의 수정 컨트롤러 실패 - dto 유효성 검사 실패")
    void updateAskControllerFail4() throws Exception {

        AskUpdateDto updateDto = AskUpdateDto.builder()
            .build();

        mockMvc.perform(put("/api/asks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 작성한 문의 삭제 컨트롤러")
    void deleteAskController() throws Exception {

        mockMvc.perform(delete("/api/asks/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("삭제되었습니다."));

        verify(askService, times(1)).deleteAsk("memberId", 1L);
    }

    @Test
    @DisplayName("회원이 작성한 문의 삭제 컨트롤러 실패 - 로그인 x")
    void deleteAskControllerFail1() throws Exception {

        mockMvc.perform(delete("/api/asks/1"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("회원이 작성한 문의 삭제 컨트롤러 실패 - url 경로 다름")
    void deleteAskControllerFail2() throws Exception {

        mockMvc.perform(delete("/api/ask"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
}