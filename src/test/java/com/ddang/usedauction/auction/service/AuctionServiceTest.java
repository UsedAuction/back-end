package com.ddang.usedauction.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.TransactionType;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.dto.AuctionServiceDto;
import com.ddang.usedauction.auction.exception.AuctionException;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.category.exception.CategoryException;
import com.ddang.usedauction.category.repository.CategoryRepository;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.image.service.ImageService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(SpringExtension.class)
class AuctionServiceTest {

    @MockBean
    AuctionRepository auctionRepository;

    @MockBean
    CategoryRepository categoryRepository;

    @MockBean
    MemberRepository memberRepository;

    @MockBean
    ImageService imageService;

    @MockBean
    RedisTemplate<String, AuctionServiceDto> auctionRedisTemplate;

    @MockBean
    ValueOperations<String, AuctionServiceDto> valueOperations;

    AuctionService auctionService;
    MockMultipartFile thumbnail;
    List<MultipartFile> imageList;
    String memberId;
    AuctionCreateDto.Request createDto;
    Member member;
    Category parentCategory;
    Category childCategory;
    Image image;
    List<Image> uploadImageList;
    Auction auction;

    @BeforeEach
    void before() {

        auctionService = new AuctionService(auctionRepository, categoryRepository, memberRepository,
            imageService, auctionRedisTemplate);

        when(auctionRedisTemplate.opsForValue()).thenReturn(valueOperations);

        thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png", MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        imageList = List.of(mockImage, mockImage, mockImage, mockImage, mockImage);

        memberId = "test";

        createDto = AuctionCreateDto.Request.builder()
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
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        member = Member.builder()
            .memberId("test")
            .point(2000)
            .email("test@naver.com")
            .passWord("1234")
            .siteAlarm(true)
            .build();

        parentCategory = Category.builder()
            .categoryName("category1")
            .build();

        childCategory = Category.builder()
            .categoryName("category2")
            .parentId(1L)
            .build();

        image = Image.builder()
            .imageName("imageName")
            .imageType(ImageType.THUMBNAIL)
            .imageUrl("url")
            .build();

        Image normalImage = Image.builder()
            .imageName("imageName2")
            .imageType(ImageType.NORMAL)
            .imageUrl("url")
            .build();
        uploadImageList = List.of(normalImage, normalImage, normalImage, normalImage, normalImage);

        auction = Auction.builder()
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
    }

    @Test
    @DisplayName("경매글 생성")
    void createAuction() {

        when(memberRepository.findByMemberId(any())).thenReturn(Optional.of(member));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(childCategory));
        when(imageService.uploadThumbnail(any())).thenReturn(image);
        when(imageService.uploadImageList(anyList())).thenReturn(uploadImageList);
        when(auctionRepository.save(any())).thenReturn(auction);

        AuctionServiceDto serviceDto = auctionService.createAuction(thumbnail, imageList, memberId,
            createDto);

        assertThat(serviceDto.getCurrentPrice()).isEqualTo(3000);
    }

    @Test
    @DisplayName("경매글 생성 실패 - 이미지가 6개보다 많은 경우")
    void createAuctionFail1() {

        imageList = new ArrayList<>(imageList);
        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        imageList.add(mockImage);

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, memberId,
            createDto)).isInstanceOf(AuctionException.class);
    }

    @Test
    @DisplayName("경매글 생성 실패 - 경매가 일주일보다 더 길게 진행되는 경우")
    void createAuctionFail2() {

        createDto = createDto.toBuilder()
            .endedAt(LocalDateTime.now().plusDays(8))
            .build();

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, memberId,
            createDto)).isInstanceOf(AuctionException.class);
    }

    @Test
    @DisplayName("경매글 생성 실패 - 즉시 구매가가 입찰 시작가보다 작거나 같은 경우")
    void createAuctionFail3() {

        createDto = createDto.toBuilder()
            .instantPrice(3000)
            .build();

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, memberId,
            createDto)).isInstanceOf(AuctionException.class);
    }

    @Test
    @DisplayName("경매글 생성 실패 - 존재하지 않는 회원인 경우")
    void createAuctionFail4() {

        when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, memberId,
            createDto)).isInstanceOf(MemberException.class);
    }

    @Test
    @DisplayName("경매글 생성 실패 - 존재하지 않는 카테고리인 경우")
    void createAuctionFail5() {

        when(memberRepository.findByMemberId(any())).thenReturn(Optional.of(member));
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, memberId,
            createDto)).isInstanceOf(CategoryException.class);
    }
}