package com.ddang.usedauction.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.TransactionType;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
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
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import com.ddang.usedauction.point.type.PointType;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
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

    @InjectMocks
    private AuctionService auctionService;

    @Test
    @DisplayName("경매글 단건 조회")
    void getAuction() {

        Auction auction = Auction.builder()
            .title("title")
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        Auction result = auctionService.getAuction(1L);

        assertThat(result.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("경매글 단건 조회 실패 - 등록되지 않은 경매글")
    void getAuctionFail1() {

        when(auctionRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.getAuction(1L)).isInstanceOf(
            NullPointerException.class);
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

        assertThat(resultList.getTotalElements()).isEqualTo(2);
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
            .transactionType(TransactionType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        when(memberRepository.findByMemberId("test")).thenReturn(Optional.of(member));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(childCategory));
        when(imageService.uploadThumbnail(thumbnail)).thenReturn(image);
        when(imageService.uploadImageList(imageList)).thenReturn(uploadImageList);
        when(auctionRepository.save(auction)).thenReturn(auction);

        Auction result = auctionService.createAuction(thumbnail, imageList, "test",
            createDto);

        assertThat(result.getTitle()).isEqualTo("title");
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
            mockImage);

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

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, "test",
            createDto)).isInstanceOf(ImageCountOutOfBoundsException.class);
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
            .transactionType(TransactionType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, "test",
            createDto)).isInstanceOf(AuctionMaxDateOutOfBoundsException.class);
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
            .transactionType(TransactionType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, "test",
            createDto)).isInstanceOf(StartPriceOutOfBoundsException.class);
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
            .transactionType(TransactionType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        when(memberRepository.findByMemberId("test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, "test",
            createDto)).isInstanceOf(NullPointerException.class);
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
            .transactionType(TransactionType.ALL)
            .productColor("color")
            .childCategoryId(2L)
            .parentCategoryId(1L)
            .build();

        Member member = Member.builder()
            .memberId("test")
            .build();

        when(memberRepository.findByMemberId("test")).thenReturn(Optional.of(member));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.createAuction(thumbnail, imageList, "test",
            createDto)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("구매 확정")
    void confirmAuction() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Auction auction = Auction.builder()
            .title("title")
            .auctionState(AuctionState.END)
            .build();

        Member buyer = Member.builder()
            .memberId("buyer")
            .point(5000)
            .build();

        Member seller = Member.builder()
            .memberId("seller")
            .point(1000)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId("buyer")).thenReturn(Optional.of(buyer));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(seller));

        auctionService.confirmAuction(1L, "buyer", confirmDto);

        PointHistory buyerPointHistory = PointHistory.builder()
            .curPointAmount(buyer.getPoint())
            .pointType(PointType.USE)
            .pointAmount(confirmDto.getPrice())
            .member(buyer)
            .build();

        PointHistory sellerPointHistory = PointHistory.builder()
            .curPointAmount(seller.getPoint())
            .pointType(PointType.GET)
            .pointAmount(confirmDto.getPrice())
            .member(seller)
            .build();

        Transaction buyerTransaction = Transaction.builder()
            .auction(auction)
            .member(buyer)
            .price(confirmDto.getPrice())
            .transType(TransType.BUY)
            .build();

        Transaction sellerTransaction = Transaction.builder()
            .auction(auction)
            .member(seller)
            .price(confirmDto.getPrice())
            .transType(TransType.SELL)
            .build();

        verify(pointRepository, times(1)).save(buyerPointHistory);
        verify(pointRepository, times(1)).save(sellerPointHistory);
        verify(transactionRepository, times(1)).save(buyerTransaction);
        verify(transactionRepository, times(1)).save(sellerTransaction);
    }

    @Test
    @DisplayName("구매 확정 실패 - 없는 경매글")
    void confirmAuctionFail1() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> auctionService.confirmAuction(1L, "test", confirmDto)).isInstanceOf(
            NullPointerException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 없는 회원")
    void confirmAuctionFail2() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Auction auction = Auction.builder()
            .title("title")
            .auctionState(AuctionState.END)
            .build();

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> auctionService.confirmAuction(1L, "test", confirmDto)).isInstanceOf(
            NullPointerException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 진행중인 경매인 경우")
    void confirmAuctionFail3() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Auction auction = Auction.builder()
            .title("title")
            .auctionState(AuctionState.CONTINUE)
            .build();

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));

        assertThatThrownBy(
            () -> auctionService.confirmAuction(1L, "test", confirmDto)).isInstanceOf(
            IllegalStateException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 구매자 포인트가 부족한 경우")
    void confirmAuctionFail4() {

        AuctionConfirmDto.Request confirmDto = AuctionConfirmDto.Request.builder()
            .price(1000)
            .sellerId(2L)
            .build();

        Auction auction = Auction.builder()
            .title("title")
            .auctionState(AuctionState.END)
            .build();

        Member buyer = Member.builder()
            .memberId("buyer")
            .point(500)
            .build();

        when(auctionRepository.findById(any())).thenReturn(Optional.of(auction));
        when(memberRepository.findByMemberId(any())).thenReturn(Optional.of(buyer));

        assertThatThrownBy(
            () -> auctionService.confirmAuction(1L, "test", confirmDto)).isInstanceOf(
            MemberPointOutOfBoundsException.class);
    }
}