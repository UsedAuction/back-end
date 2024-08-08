package com.ddang.usedauction.auction.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.TransactionType;
import com.ddang.usedauction.auction.dto.AuctionServiceDto;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.member.domain.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuctionService auctionService;

    MockMvc mockMvc;
    MockMultipartFile thumbnail;
    MockMultipartFile mockImage;
    List<MultipartFile> imageList;
    String createDto;
    AuctionServiceDto auctionServiceDto;
    String memberId;
    Page<AuctionServiceDto> auctionPageList;
    Pageable pageable;

    @BeforeEach
    void before() {

        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();

        thumbnail = new MockMultipartFile("thumbnail", "thumbnail.png", MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        mockImage = new MockMultipartFile("imageList", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        imageList = List.of(mockImage, mockImage, mockImage, mockImage, mockImage);

        createDto = "{\n"
            + "    \"title\": \"test\",\n"
            + "    \"contactPlace\": \"place\",\n"
            + "    \"deliveryPrice\": \"price\",\n"
            + "    \"deliveryType\": \"prepay\",\n"
            + "    \"endedAt\": \"2024-08-09 00:00\",\n"
            + "    \"instantPrice\": 4000,\n"
            + "    \"productName\": \"name\",\n"
            + "    \"productStatus\": 3.5,\n"
            + "    \"startPrice\": 3000,\n"
            + "    \"productDescription\": \"설명\",\n"
            + "    \"transactionType\": \"all\",\n"
            + "    \"productColor\": \"color\",\n"
            + "    \"childCategoryId\": 2,\n"
            + "    \"parentCategoryId\": 1\n"
            + "}";

        Category parentCategory = Category.builder()
            .categoryName("category1")
            .build();

        Category childCategory = Category.builder()
            .categoryName("category2")
            .parentId(1L)
            .build();

        auctionServiceDto = AuctionServiceDto.builder()
            .title("test")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.of(2024, 8, 9, 0, 0))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(3000)
            .productDescription("설명")
            .transactionType(TransactionType.ALL)
            .productColor("color")
            .childCategory(childCategory.toServiceDto())
            .parentCategory(parentCategory.toServiceDto())
            .build();

        memberId = "test";

        Member member = Member.builder()
            .memberId("test")
            .point(2000)
            .email("test@naver.com")
            .passWord("1234")
            .siteAlarm(true)
            .build();

        Auction auction = Auction.builder()
            .auctionState(AuctionState.CONTINUE)
            .currentPrice(3000)
            .productDescription("description")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.of(2024, 8, 9, 0, 0))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .childCategory(childCategory)
            .seller(member)
            .productColor("color")
            .parentCategory(parentCategory)
            .title("title")
            .transactionType(TransactionType.ALL)
            .build();

        List<AuctionServiceDto> auctionList = List.of(auction.toServiceDto());
        pageable = PageRequest.of(0, 10);
        auctionPageList = new PageImpl<>(auctionList, pageable, auctionList.size());
    }

    @Test
    @DisplayName("경매글 단건 조회 컨트롤러")
    void getAuctionController() throws Exception {

        when(auctionService.getAuction(anyLong())).thenReturn(auctionServiceDto);

        mockMvc.perform(get("/api/auctions/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("경매글 단건 조회 컨트롤러 실패 - url 경로 다름")
    void getAuctionControllerFail1() throws Exception {

        mockMvc.perform(get("/api/auction/1"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("경매글 단건 조회 컨트롤러 실패 - pathVariable 유효성 검증 실패")
    void getAuctionControllerFail2() throws Exception {

        mockMvc.perform(get("/api/auctions/0"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("경매글 리스트 조회 컨트롤러")
    void getAuctionListController() throws Exception {

        when(auctionService.getAuctionList(any(), any(), any(), any())).thenReturn(auctionPageList);

        mockMvc.perform(get("/api/auctions?word=&category=&sorted="))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("경매글 리스트 조회 컨트롤러 실패 - url 경로 다름")
    void getAuctionListControllerFail1() throws Exception {

        mockMvc.perform(get("/api/auction?word=&category=&sorted="))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("경매글 리스트 조회 컨트롤러 실패 - param 입력 x")
    void getAuctionListControllerFail2() throws Exception {

        mockMvc.perform(get("/api/auctions?word=&category="))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러")
    void createAuctionController() throws Exception {

        when(auctionService.createAuction(any(), anyList(), any(), any())).thenReturn(
            auctionServiceDto);

        mockMvc.perform(multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createDto.getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - url 경로 다름")
    void createAuctionControllerFail1() throws Exception {

        mockMvc.perform(multipart("/api/auction")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createDto.getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - 필수 RequestPart 값 존재하지 않음")
    void createAuctionControllerFail2() throws Exception {

        mockMvc.perform(multipart("/api/auctions")
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createDto.getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - 이미지가 아닌 다른 파일인 경우")
    void createAuctionControllerFail3() throws Exception {

        thumbnail = new MockMultipartFile("thumbnail", "thumbnail.txt", MediaType.TEXT_PLAIN_VALUE,
            "thumbnail".getBytes());

        mockMvc.perform(multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createDto.getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - request 유효성 검사 실패")
    void createAuctionControllerFail4() throws Exception {

        mockMvc.perform(multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createDto.getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].status").value(400));
    }
}