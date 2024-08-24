package com.ddang.usedauction.auction.controller;


import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ddang.usedauction.annotation.WithCustomMockUser;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.ReceiveType;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.dto.AuctionRecentDto;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.category.domain.Category;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest({AuctionController.class, SecurityConfig.class})
class AuctionControllerTest {

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
    private AuctionService auctionService;

    Auction auction;

    @BeforeEach
    void setup() {

        Category parentCategory = Category.builder()
            .id(1L)
            .categoryName("category1")
            .build();

        Category childCategory = Category.builder()
            .id(2L)
            .categoryName("category2")
            .build();

        Member member = Member.builder()
            .id(1L)
            .memberId("test")
            .build();

        Image image = Image.builder()
            .id(1L)
            .imageName("image1")
            .imageType(ImageType.THUMBNAIL)
            .imageUrl("url1")
            .build();

        Image image2 = Image.builder()
            .id(2L)
            .imageName("image1")
            .imageType(ImageType.NORMAL)
            .imageUrl("url1")
            .build();

        List<Image> imageList = List.of(image, image2);

        auction = Auction.builder()
            .id(1L)
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
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategory(childCategory)
            .parentCategory(parentCategory)
            .seller(member)
            .imageList(imageList)
            .build();
    }

    @Test
    @DisplayName("경매글 단건 조회 컨트롤러")
    @WithAnonymousUser
    void getAuctionController() throws Exception {

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
    @WithAnonymousUser
    void getAuctionControllerFail2() throws Exception {

        mockMvc.perform(get("/api/auctions/0"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("경매글 리스트 조회 컨트롤러")
    void getAuctionListController() throws Exception {

        List<Auction> auctionList = List.of(auction);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Auction> auctionPageList = new PageImpl<>(auctionList, pageable, auctionList.size());

        when(auctionService.getAuctionList(null, null, null, pageable)).thenReturn(auctionPageList);

        mockMvc.perform(get("/api/auctions"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("title"));
    }

    @Test
    @DisplayName("경매글 리스트 조회 컨트롤러 실패 - url 경로 다름")
    void getAuctionListControllerFail1() throws Exception {

        mockMvc.perform(get("/api/auction"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("top5 경매 리스트 조회 컨트롤러")
    void getTop5Controller() throws Exception {

        Member member1 = Member.builder()
            .memberId("test1")
            .build();

        Member member2 = Member.builder()
            .memberId("test2")
            .build();

        Bid bid1 = Bid.builder()
            .member(member1)
            .auction(auction)
            .build();

        Bid bid2 = Bid.builder()
            .member(member2)
            .auction(auction)
            .build();

        Auction auction1 = auction.toBuilder()
            .bidList(List.of(bid1, bid2))
            .build();

        Auction auction2 = auction.toBuilder()
            .bidList(List.of(bid1))
            .build();

        when(auctionService.getTop5()).thenReturn(List.of(auction1, auction2));

        mockMvc.perform(get("/api/auctions/top5"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].bidList[0].memberId").value("test1"))
            .andExpect(jsonPath("$[0].bidList[1].memberId").value("test2"))
            .andExpect(jsonPath("$[1].bidList[0].memberId").value("test1"));
    }

    @Test
    @DisplayName("top5 경매 리스트 조회 컨트롤러 실패 - url 경로 다름")
    void getTop5ControllerFail1() throws Exception {

        mockMvc.perform(get("/api/auction/top5"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("최근 본 경매 리스트 조회 컨트롤러")
    void getAuctionRecentListController() throws Exception {

        AuctionRecentDto auctionRecentDto = AuctionRecentDto.builder()
            .auctionTitle("title")
            .build();
        List<AuctionRecentDto> auctionRecentDtoList = List.of(auctionRecentDto);

        when(auctionService.getAuctionRecentList()).thenReturn(auctionRecentDtoList);

        mockMvc.perform(get("/api/auctions/recent"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].auctionTitle").value("title"));
    }

    @Test
    @DisplayName("최근 본 경매 리스트 조회 컨트롤러 실패 - url 경로 다름")
    void getAuctionRecentListControllerFail1() throws Exception {

        mockMvc.perform(get("/api/auction/recent"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("경매글 생성 컨트롤러")
    void createAuctionController() throws Exception {

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(3000)
            .productDescription("설명")
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        when(auctionService.createAuction(
            argThat(arg -> arg.getName().equals("thumbnail")),
            argThat(arg -> arg.get(0).getName().equals("imageList")),
            argThat(arg -> arg.equals("test@naver.com")),
            argThat(arg -> arg.getTitle().equals("title")))).thenReturn(auction);

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
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

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(3000)
            .productDescription("설명")
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        mockMvc.perform(multipart("/api/auction")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("경매글 생성 컨트롤러 실패 - 필수 RequestPart 값 존재하지 않음")
    void createAuctionControllerFail2() throws Exception {

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(3000)
            .productDescription("설명")
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        mockMvc.perform(multipart("/api/auctions")
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("경매글 생성 컨트롤러 실패 - 이미지가 아닌 다른 파일인 경우")
    void createAuctionControllerFail3() throws Exception {

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(3000)
            .productDescription("설명")
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        mockMvc.perform(multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("경매글 생성 컨트롤러 실패 - request 유효성 검사 실패")
    void createAuctionControllerFail4() throws Exception {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(3000)
            .productDescription("설명")
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        mockMvc.perform(multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(mockImage)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("경매글 생성 컨트롤러 실패 - 로그인 x")
    void createAuctionControllerFail5() throws Exception {

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile mockImage = new MockMultipartFile("imageList", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(3000)
            .productDescription("설명")
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        when(auctionService.createAuction(
            argThat(arg -> arg.getName().equals("thumbnail")),
            argThat(arg -> arg.get(0).getName().equals("imageList")),
            argThat(arg -> arg.equals("test@naver.com")),
            argThat(arg -> arg.getTitle().equals("title")))).thenReturn(auction);

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/api/auctions")
                .file(thumbnail)
                .file(mockImage)
                .file(new MockMultipartFile("createDto", "", "application/json",
                    objectMapper.writeValueAsString(createDto).getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
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
    @WithCustomMockUser
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
    @WithCustomMockUser
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

    @Test
    @DisplayName("구매 확정 컨트롤러 실패 - 로그인 x")
    void confirmAuctionControllerFail4() throws Exception {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        mockMvc.perform(post("/api/auctions/1/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDto)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("즉시 구매 컨트롤러")
    void instancePurchaseAuctionController() throws Exception {

        mockMvc.perform(post("/api/auctions/1"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("즉시 구매 컨트롤러 실패 - url 경로 다름")
    void instancePurchaseAuctionControllerFail1() throws Exception {

        mockMvc.perform(post("/api/auction/1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @WithCustomMockUser
    @DisplayName("즉시 구매 컨트롤러 실패 - PathVariable 유효성 검증 실패")
    void instancePurchaseAuctionControllerFail2() throws Exception {

        mockMvc.perform(post("/api/auctions/0"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("즉시 구매 컨트롤러 실패 - 로그인 x")
    void instancePurchaseAuctionControllerFail3() throws Exception {

        mockMvc.perform(post("/api/auctions/1"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}