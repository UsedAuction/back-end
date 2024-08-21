package com.ddang.usedauction.auction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.ReceiveType;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.dto.AuctionEndDto;
import com.ddang.usedauction.auction.dto.AuctionRecentDto;
import com.ddang.usedauction.auction.exception.AuctionMaxDateOutOfBoundsException;
import com.ddang.usedauction.auction.exception.ImageCountOutOfBoundsException;
import com.ddang.usedauction.auction.exception.MemberPointOutOfBoundsException;
import com.ddang.usedauction.auction.exception.StartPriceOutOfBoundsException;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.category.repository.CategoryRepository;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.domain.ImageType;
import com.ddang.usedauction.image.service.ImageService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.service.NotificationService;
import com.ddang.usedauction.point.repository.PointRepository;
import com.ddang.usedauction.point.type.PointType;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private AuctionRedisService auctionRedisService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RedisTemplate<String, AuctionRecentDto> redisTemplate;

    @Mock
    private ListOperations<String, AuctionRecentDto> listOperations;

    @InjectMocks
    private AuctionService auctionService;

    @Test
    @DisplayName("경매글 단건 조회")
    void getAuction() {

        Image image = Image.builder()
            .imageType(ImageType.THUMBNAIL)
            .build();
        List<Image> imageList = List.of(image);

        Auction auction = Auction.builder()
            .id(1L)
            .title("title")
            .imageList(imageList)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(redisTemplate.opsForList()).thenReturn(listOperations);

        Auction result = auctionService.getAuction(1L);

        assertEquals("title", result.getTitle());
        verify(redisTemplate.opsForList(), times(1)).leftPush(
            argThat(arg -> arg.equals("recently::test@example.com")),
            argThat(arg -> arg.getAuctionTitle().equals("title")));
        verify(redisTemplate.opsForList(), times(1)).trim("recently::test@example.com", 0, 4);
        verify(redisTemplate, times(1)).expire("recently::test@example.com", Duration.ofHours(12));
    }

    @Test
    @DisplayName("경매글 단건 조회 실패 - 등록되지 않은 경매글")
    void getAuctionFail1() {

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> auctionService.getAuction(1L));
    }

    @Test
    @DisplayName("경매글 리스트 조회")
    void getAuctionList() {

        Pageable pageable = PageRequest.of(0, 10);

        Member member = Member.builder()
            .memberId("test1")
            .build();

        Bid bid = Bid.builder()
            .member(member)
            .build();

        Category category1 = Category.builder()
            .categoryName("category1")
            .build();

        Category category2 = Category.builder()
            .categoryName("category2")
            .build();

        Auction auction1 = Auction.builder()
            .title("title")
            .parentCategory(category1)
            .currentPrice(1000)
            .endedAt(LocalDateTime.now())
            .bidList(List.of(bid))
            .build();

        Auction auction2 = Auction.builder()
            .title("abcd")
            .parentCategory(category2)
            .currentPrice(2000)
            .endedAt(LocalDateTime.now().plusDays(1))
            .build();

        List<Auction> auctionList = List.of(auction1, auction2);
        Page<Auction> auctionPageList = new PageImpl<>(auctionList, pageable, auctionList.size());

        when(auctionRepository.findAllByOptions(null, null, null, pageable)).thenReturn(
            auctionPageList);

        Page<Auction> resultList = auctionService.getAuctionList(null, null, null, pageable);

        assertEquals(2, resultList.getTotalElements());
    }

    @Test
    @DisplayName("최근 본 경매 리스트 조회")
    void getAuctionRecentList() {

        AuctionRecentDto auctionRecentDto = AuctionRecentDto.builder()
            .auctionTitle("title")
            .build();
        List<AuctionRecentDto> auctionRecentDtoList = List.of(auctionRecentDto);

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range("recently::test@example.com", 0, 4)).thenReturn(
            auctionRecentDtoList);

        List<AuctionRecentDto> auctionRecentList = auctionService.getAuctionRecentList();

        assertEquals("title", auctionRecentList.get(0).getAuctionTitle());
    }

    @Test
    @DisplayName("경매글 생성")
    void createAuction() {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> imageList = List.of(mockImage, mockImage);

        Member member = Member.builder()
            .memberId("test")
            .build();

        Category parentCategory = Category.builder()
            .categoryName("category1")
            .build();

        Category childCategory = Category.builder()
            .categoryName("category2")
            .build();

        Image image = Image.builder()
            .imageName("imageName")
            .imageType(ImageType.THUMBNAIL)
            .imageUrl("url")
            .build();

        Image normalImage1 = Image.builder()
            .imageName("imageName2")
            .imageType(ImageType.NORMAL)
            .imageUrl("url")
            .build();

        Image normalImage2 = Image.builder()
            .imageName("imageName2")
            .imageType(ImageType.NORMAL)
            .imageUrl("url")
            .build();
        List<Image> uploadImageList = List.of(normalImage1, normalImage2);

        Auction auction = Auction.builder()
            .title("title")
            .parentCategory(parentCategory)
            .childCategory(childCategory)
            .imageList(uploadImageList)
            .seller(member)
            .build();

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
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        when(memberRepository.findByMemberId("test")).thenReturn(Optional.of(member));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(childCategory));
        when(imageService.uploadThumbnail(thumbnail)).thenReturn(image);
        when(imageService.uploadImageList(imageList)).thenReturn(uploadImageList);
        when(auctionRepository.save(argThat(arg -> arg.getTitle().equals("title")))).thenReturn(
            auction);

        Auction result = auctionService.createAuction(thumbnail, imageList, "test",
            createDto);

        assertEquals("title", result.getTitle());
    }

    @Test
    @DisplayName("경매글 생성 실패 - 이미지가 6개보다 많은 경우")
    void createAuctionFail1() {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> imageList = List.of(mockImage, mockImage, mockImage, mockImage,
            mockImage, mockImage);

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
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        assertThrows(ImageCountOutOfBoundsException.class,
            () -> auctionService.createAuction(thumbnail, imageList, "test",
                createDto));
    }

    @Test
    @DisplayName("경매글 생성 실패 - 경매가 일주일보다 더 길게 진행되는 경우")
    void createAuctionFail2() {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> imageList = List.of(mockImage, mockImage, mockImage, mockImage);

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(8))
            .instantPrice(4000)
            .productName("name")
            .productStatus(3.5)
            .startPrice(1000)
            .productDescription("설명")
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        assertThrows(AuctionMaxDateOutOfBoundsException.class,
            () -> auctionService.createAuction(thumbnail, imageList, "test",
                createDto));
    }

    @Test
    @DisplayName("경매글 생성 실패 - 즉시 구매가가 입찰 시작가보다 작거나 같은 경우")
    void createAuctionFail3() {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> imageList = List.of(mockImage, mockImage, mockImage, mockImage);

        AuctionCreateDto.Request createDto = AuctionCreateDto.Request.builder()
            .title("title")
            .contactPlace("place")
            .deliveryPrice("price")
            .deliveryType(DeliveryType.PREPAY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .instantPrice(500)
            .productName("name")
            .productStatus(3.5)
            .startPrice(1000)
            .productDescription("설명")
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        assertThrows(StartPriceOutOfBoundsException.class,
            () -> auctionService.createAuction(thumbnail, imageList, "test",
                createDto));
    }

    @Test
    @DisplayName("경매글 생성 실패 - 존재하지 않는 회원인 경우")
    void createAuctionFail4() {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> imageList = List.of(mockImage, mockImage, mockImage, mockImage);

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
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        when(memberRepository.findByMemberId("test")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> auctionService.createAuction(thumbnail, imageList, "test",
                createDto));
    }

    @Test
    @DisplayName("경매글 생성 실패 - 존재하지 않는 카테고리인 경우")
    void createAuctionFail5() {

        MockMultipartFile thumbnail = new MockMultipartFile("경매 썸네일 이미지", "thumbnail.png",
            MediaType.IMAGE_PNG_VALUE,
            "thumbnail".getBytes());

        MockMultipartFile mockImage = new MockMultipartFile("경매 일반 이미지", "image.png",
            MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        List<MultipartFile> imageList = List.of(mockImage, mockImage, mockImage, mockImage);

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
            .receiveType(ReceiveType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        Member member = Member.builder()
            .memberId("test")
            .build();

        when(memberRepository.findByMemberId("test")).thenReturn(Optional.of(member));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> auctionService.createAuction(thumbnail, imageList, "test",
                createDto));
    }

    @Test
    @DisplayName("구매 확정")
    void confirmAuction() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Member seller = Member.builder()
            .id(2L)
            .memberId("seller")
            .point(1000)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("title")
            .auctionState(AuctionState.END)
            .seller(seller)
            .build();

        Member buyer = Member.builder()
            .id(1L)
            .memberId("buyer")
            .point(5000)
            .build();

        Transaction transaction = Transaction.builder()
            .buyer(buyer)
            .transType(TransType.CONTINUE)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("buyer")).thenReturn(Optional.of(buyer));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(seller));
        when(transactionRepository.findByBuyerIdAndAuctionId("buyer", 1L)).thenReturn(
            Optional.of(transaction));

        auctionService.confirmAuction(1L, "buyer", confirmDto);

        verify(memberRepository, times(1)).save(argThat(arg -> arg.getPoint() == 2000));
        verify(pointRepository, times(1)).save(
            argThat(arg -> arg.getPointType().equals(PointType.USE)));
        verify(pointRepository, times(1)).save(
            argThat(arg -> arg.getPointType().equals(PointType.GET)));
        verify(transactionRepository, times(1)).save(
            argThat(arg -> arg.getTransType().equals(TransType.SUCCESS)));
    }

    @Test
    @DisplayName("구매 확정 실패 - 없는 경매글")
    void confirmAuctionFail1() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(1L, "test", confirmDto));
    }

    @Test
    @DisplayName("구매 확정 실패 - 없는 거래 내역")
    void confirmAuctionFail2() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("title")
            .auctionState(AuctionState.END)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(transactionRepository.findByBuyerIdAndAuctionId("test", 1L)).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(1L, "test", confirmDto));
    }

    @Test
    @DisplayName("구매 확정 실패 - 없는 회원")
    void confirmAuctionFail3() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("title")
            .auctionState(AuctionState.END)
            .build();

        Member buyer = Member.builder()
            .id(1L)
            .memberId("buyer")
            .point(5000)
            .build();

        Transaction transaction = Transaction.builder()
            .buyer(buyer)
            .transType(TransType.CONTINUE)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(transactionRepository.findByBuyerIdAndAuctionId("test", 1L)).thenReturn(
            Optional.of(transaction));
        when(memberRepository.findByMemberId("test")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(1L, "test", confirmDto));
    }

    @Test
    @DisplayName("구매 확정 실패 - 진행중인 경매인 경우")
    void confirmAuctionFail4() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("title")
            .auctionState(AuctionState.CONTINUE)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        assertThrows(IllegalStateException.class,
            () -> auctionService.confirmAuction(1L, "test", confirmDto));
    }

    @Test
    @DisplayName("구매 확정 실패 - 없는 거래 내역")
    void confirmAuctionFail5() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .title("title")
            .auctionState(AuctionState.END)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(transactionRepository.findByBuyerIdAndAuctionId("buyer", 1L)).thenReturn(
            Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> auctionService.confirmAuction(1L, "buyer", confirmDto));
    }

    @Test
    @DisplayName("경매 종료")
    void endAuction() {

        Member member1 = Member.builder()
            .id(1L)
            .point(2000)
            .build();

        Member member2 = Member.builder()
            .id(2L)
            .point(2000)
            .build();

        Member seller = Member.builder()
            .id(3L)
            .point(0)
            .build();

        Bid bid1 = Bid.builder()
            .id(1L)
            .member(member1)
            .bidPrice(1000)
            .build();

        Bid bid2 = Bid.builder()
            .id(2L)
            .member(member2)
            .bidPrice(2000)
            .build();

        List<Bid> bidList = List.of(bid1, bid2);

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.CONTINUE)
            .bidList(bidList)
            .seller(seller)
            .build();

        Member afterMember2 = member2.toBuilder()
            .point(0)
            .build();

        Auction afterAuction = auction.toBuilder()
            .auctionState(AuctionState.END)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.save(argThat(arg -> arg.getId().equals(2L)))).thenReturn(
            afterMember2);
        when(auctionRepository.save(argThat(arg -> arg.getId().equals(1L)))).thenReturn(
            afterAuction);

        AuctionEndDto auctionEndDto = auctionService.endAuction(1L);

        verify(transactionRepository, times(1)).save(argThat(arg -> arg.getPrice() == 2000));

        assertEquals(1, auctionEndDto.getAuctionId());
        assertEquals(2, auctionEndDto.getBuyerId());
    }

    @Test
    @DisplayName("경매 종료 실패 - 존재하지 않는 경매")
    void endAuctionFail1() {

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> auctionService.endAuction(1L));
    }

    @Test
    @DisplayName("경매 종료 실패 - 이미 종료된 경매")
    void endAuctionFail2() {

        Auction auction = Auction.builder()
            .auctionState(AuctionState.END)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        assertThrows(IllegalStateException.class, () -> auctionService.endAuction(1L));
    }

    @Test
    @DisplayName("즉시 구매")
    void instantPurchaseAuction() {

        Member seller = Member.builder()
            .id(2L)
            .build();

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.CONTINUE)
            .instantPrice(2000)
            .seller(seller)
            .build();

        Member buyer = Member.builder()
            .memberId("buyer")
            .point(2000)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("buyer")).thenReturn(Optional.of(buyer));

        auctionService.instantPurchaseAuction(1L, "buyer");

        verify(auctionRepository, times(1)).save(
            argThat(arg -> arg.getAuctionState().equals(AuctionState.END)));
        verify(memberRepository, times(1)).save(argThat(arg -> arg.getPoint() == 0));
        verify(transactionRepository, times(1)).save(argThat(arg -> arg.getPrice() == 2000));

    }

    @Test
    @DisplayName("즉시 구매 실패 - 존재하지 않는 경매")
    void instantPurchaseAuctionFail1() {

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> auctionService.instantPurchaseAuction(1L, "buyer"));
    }

    @Test
    @DisplayName("즉시 구매 실패 - 존재하지 않는 회원")
    void instantPurchaseAuctionFail2() {

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.CONTINUE)
            .instantPrice(2000)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("buyer")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
            () -> auctionService.instantPurchaseAuction(1L, "buyer"));
    }

    @Test
    @DisplayName("즉시 구매 실패 - 이미 종료된 경매")
    void instantPurchaseAuctionFail3() {

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.END)
            .instantPrice(2000)
            .build();

        Member buyer = Member.builder()
            .memberId("buyer")
            .point(2000)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("buyer")).thenReturn(Optional.of(buyer));

        assertThrows(IllegalStateException.class,
            () -> auctionService.instantPurchaseAuction(1L, "buyer"));
    }

    @Test
    @DisplayName("즉시 구매 실패 - 구매자의 포인트가 부족한 경우")
    void instantPurchaseAuctionFail4() {

        Auction auction = Auction.builder()
            .id(1L)
            .auctionState(AuctionState.CONTINUE)
            .instantPrice(2000)
            .build();

        Member buyer = Member.builder()
            .memberId("buyer")
            .point(1000)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("buyer")).thenReturn(Optional.of(buyer));

        assertThrows(MemberPointOutOfBoundsException.class,
            () -> auctionService.instantPurchaseAuction(1L, "buyer"));
    }
}