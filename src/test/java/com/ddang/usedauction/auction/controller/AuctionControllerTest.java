package com.ddang.usedauction.auction.controller;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.TransactionType;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.service.AuctionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuctionService auctionService;

    @Test
    @DisplayName("경매글 단건 조회 컨트롤러")
    void getAuctionController() throws Exception {

        Auction auction = Auction.builder()
            .title("title")
            .build();

        when(auctionService.getAuction(1L)).thenReturn(auction);

        mockMvc.perform(get("/api/auctions/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("title"));
    }

    @Test
    @DisplayName("경매글 단건 조회 컨트롤러 실패 - url 경로 다름")
    void getAuctionControllerFail1() throws Exception {

        mockMvc.perform(get("/api/auction/1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("경매글 단건 조회 컨트롤러 실패 - pathVariable 유효성 검증 실패")
    void getAuctionControllerFail2() throws Exception {

        mockMvc.perform(get("/api/auctions/0"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("경매글 리스트 조회 컨트롤러")
    void getAuctionListController() throws Exception {

        Auction auction = Auction.builder()
            .title("title")
            .build();

        List<Auction> auctionList = List.of(auction);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Auction> auctionPageList = new PageImpl<>(auctionList, pageable, auctionList.size());

        when(auctionService.getAuctionList(null, null, null, pageable)).thenReturn(auctionPageList);

        mockMvc.perform(get("/api/auctions"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("title"));
    }

    @Test
    @DisplayName("경매글 리스트 조회 컨트롤러 실패 - url 경로 다름")
    void getAuctionListControllerFail1() throws Exception {

        mockMvc.perform(get("/api/auction"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러")
    void createAuctionController() throws Exception {

        Auction auction = Auction.builder()
            .title("title")
            .build();

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> imageList = List.of(mockImage, mockImage, mockImage);

        String createRequest = "{\n"
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

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(1000)
            .productDescription("설명")
            .transactionType(TransactionType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        when(auctionService.createAuction(thumbnail, imageList, "test", createDto)).thenReturn(
            auction);

        mockMvc.perform(multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createRequest.getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("title"));
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - url 경로 다름")
    void createAuctionControllerFail1() throws Exception {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        String createRequest = "{\n"
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

        mockMvc.perform(multipart("/api/auction")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createRequest.getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - 필수 RequestPart 값 존재하지 않음")
    void createAuctionControllerFail2() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        String createRequest = "{\n"
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

        mockMvc.perform(multipart("/api/auctions")
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createRequest.getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - 이미지가 아닌 다른 파일인 경우")
    void createAuctionControllerFail3() throws Exception {

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        String createRequest = "{\n"
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

        mockMvc.perform(multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createRequest.getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - request 유효성 검사 실패")
    void createAuctionControllerFail4() throws Exception {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        String createRequest = "{\n"
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

        mockMvc.perform(multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    createRequest.getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("구매 확정 컨트롤러")
    void confirmAuctionController() throws Exception {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        mockMvc.perform(post("/api/auctions/1/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDto)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("구매 확정 컨트롤러 실패 - url 경로 다름")
    void confirmAuctionControllerFail1() throws Exception {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        mockMvc.perform(post("/api/auction/1/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDto)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("구매 확정 컨트롤러 실패 - pathVariable 유효성 검사 실패")
    void confirmAuctionControllerFail2() throws Exception {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        mockMvc.perform(post("/api/auctions/0/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("구매 확정 컨트롤러 실패 - dto 유효성 검사 실패")
    void confirmAuctionControllerFail3() throws Exception {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(0)
            .sellerId(0L)
            .build();

        mockMvc.perform(post("/api/auctions/0/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}