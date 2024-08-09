package com.ddang.usedauction.auction.service;

import com.ddang.usedauction.aop.RedissonLock;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.dto.AuctionServiceDto;
import com.ddang.usedauction.auction.exception.AuctionErrorCode;
import com.ddang.usedauction.auction.exception.AuctionException;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.category.exception.CategoryErrorCode;
import com.ddang.usedauction.category.exception.CategoryException;
import com.ddang.usedauction.category.repository.CategoryRepository;
import com.ddang.usedauction.config.CacheName;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.service.ImageService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.exception.MemberErrorCode;
import com.ddang.usedauction.member.exception.MemberException;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import com.ddang.usedauction.point.type.PointType;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final PointRepository pointRepository;
    private final ImageService imageService;
    private final RedisTemplate<String, AuctionServiceDto> auctionRedisTemplate;

    /**
     * 경매글 단건 조회
     *
     * @param auctionId 조회할 경매글 PK
     * @return 조회된 경매글 serviceDto
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#auctionId", value = CacheName.AUCTION_CACHE_NAME)
    public AuctionServiceDto getAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AuctionException(AuctionErrorCode.NOT_FOUND_AUCTION));

        return auction.toServiceDto();
    }

    /**
     * 경매글 리스트 조회
     *
     * @param word     검색어
     * @param category 카테고리
     * @param sorted   정렬 방법
     * @param pageable 페이징
     * @return 페이징 처리된 경매 서비스 dto
     */
    @Transactional(readOnly = true)
    public Page<AuctionServiceDto> getAuctionList(String word, String category, String sorted,
        Pageable pageable) {

        final String VIEW = "view";

        Page<Auction> auctionPageList = auctionRepository.findAllByOptions(word, category, sorted,
            pageable);

        if (sorted != null && sorted.equals(VIEW)) { // 경메에 참여한 회원순으로 정렬해야하는 경우
            List<AuctionServiceDto> auctionServiceDtoList = auctionPageList.stream()
                .sorted(
                    (o1, o2) -> Math.toIntExact(o2.getBidMemberCount() - o1.getBidMemberCount()))
                .map(Auction::toServiceDto)
                .toList();

            return new PageImpl<>(auctionServiceDtoList, pageable,
                auctionPageList.getTotalElements());
        }

        return auctionPageList.map(Auction::toServiceDto);
    }

    /**
     * 경매글 생성 서비스
     *
     * @param thumbnail 대표 이미지
     * @param imageList 대표 이미지를 제외한 이미지 리스트
     * @param memberId  경매글 작성자
     * @param createDto 경매글 작성 정보
     * @return 작성된 경매글의 serviceDto
     */
    public AuctionServiceDto createAuction(MultipartFile thumbnail, List<MultipartFile> imageList,
        String memberId, AuctionCreateDto.Request createDto) {

        if (imageList != null && imageList.size() > 5) { // 썸네일 포함 6개 초과인 경우
            throw new AuctionException(AuctionErrorCode.TOO_MANY_IMAGE);
        }

        if (createDto.getEndedAt()
            .isAfter(LocalDateTime.now().plusDays(7))) { // 경매 끝나는 날짜가 일주일 초과되는 경우
            throw new AuctionException(AuctionErrorCode.END_DATE_IS_AFTER_7);
        }

        if (createDto.getEndedAt().isBefore(LocalDateTime.now())) { // 경매 끝나는 날짜가 현재 날짜보다 이전인 경우
            throw new AuctionException(AuctionErrorCode.END_DATE_IS_BEFORE_NOW);
        }

        if (createDto.getInstantPrice()
            <= createDto.getStartPrice()) { // 즉시 구매가가 입찰 시작가보다 적거나 같은 경우
            throw new AuctionException(AuctionErrorCode.LOW_PRICE);
        }

        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        Category parentCategory = categoryRepository.findById(createDto.getParentCategoryId())
            .orElseThrow(() -> new CategoryException(CategoryErrorCode.NOT_FOUND_CATEGORY));

        Category childCategory = categoryRepository.findById(createDto.getChildCategoryId())
            .orElseThrow(() -> new CategoryException(CategoryErrorCode.NOT_FOUND_CATEGORY));

        Auction auction = Auction.builder()
            .title(createDto.getTitle())
            .auctionState(AuctionState.CONTINUE)
            .productName(createDto.getProductName())
            .productColor(createDto.getProductColor())
            .productStatus(createDto.getProductStatus())
            .productDescription(createDto.getProductDescription())
            .transactionType(createDto.getTransactionType())
            .contactPlace(createDto.getContactPlace())
            .deliveryType(createDto.getDeliveryType())
            .deliveryPrice(createDto.getDeliveryPrice())
            .currentPrice(createDto.getStartPrice())
            .startPrice(createDto.getStartPrice())
            .instantPrice(createDto.getInstantPrice())
            .endedAt(createDto.getEndedAt())
            .seller(member)
            .parentCategory(parentCategory)
            .childCategory(childCategory)
            .build();

        List<Image> images = new ArrayList<>();
        Image thumnailImage = imageService.uploadThumbnail(thumbnail);
        images.add(thumnailImage);

        if (imageList != null && !imageList.isEmpty()) {
            List<Image> normalImageList = imageService.uploadImageList(imageList);
            images.addAll(normalImageList);
        }

        addImageList(images, auction);

        Auction savedAuction = auctionRepository.save(auction);

        AuctionServiceDto serviceDto = savedAuction.toServiceDto();
        String auctionKey = CacheName.AUCTION_CACHE_NAME + "::" + serviceDto.getId();
        auctionRedisTemplate.opsForValue().set(auctionKey, serviceDto); // redis에 저장

        return serviceDto;
    }

    /**
     * 경매 종료 서비스
     *
     * @param auctionId 종료할 경매 PK
     */
    @RedissonLock("#auctionId")
    @CacheEvict(key = "#auctionId", value = CacheName.AUCTION_CACHE_NAME)
    public Map<String, Long> endAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AuctionException(AuctionErrorCode.NOT_FOUND_AUCTION));

        if (auction.getAuctionState().equals(AuctionState.END)) { // 이미 종료된 경매인 경우
            throw new AuctionException(AuctionErrorCode.ALREADY_END_AUCTION);
        }

        List<Bid> bidList = auction.getBidList();
        Bid bid = bidList.stream()
            .max(Comparator.comparing(Bid::getBidPrice))
            .orElse(null);

        Member buyer = null; // 입찰자
        if (bid != null) {
            Member member = bid.getMember();
            member = member.toBuilder()
                .point(member.getPoint() - bid.getBidPrice()) // 입찰자 포인트 차감
                .build();

            buyer = memberRepository.save(member);
        }

        auction = auction.toBuilder()
            .auctionState(AuctionState.END) // 경매 종료 처리
            .build();
        Auction savedAuction = auctionRepository.save(auction);

        Map<String, Long> auctionAndMemberMap = new HashMap<>();
        auctionAndMemberMap.put("auction", savedAuction.getId());
        auctionAndMemberMap.put("buyer", buyer != null ? buyer.getId() : null);
        auctionAndMemberMap.put("seller", savedAuction.getSeller().getId());

        return auctionAndMemberMap;
    }

    /**
     * 구매 확정 서비스
     *
     * @param auctionId  경매글 PK
     * @param memberId   회원 id
     * @param confirmDto 구매 확정 정보
     */
    @RedissonLock("#auctionId")
    public void confirmAuction(Long auctionId, String memberId,
        AuctionConfirmDto.Request confirmDto) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AuctionException(AuctionErrorCode.NOT_FOUND_AUCTION));

        if (auction.getAuctionState().equals(AuctionState.CONTINUE)) { // 아직 진행중인 경매인 경우
            throw new AuctionException(AuctionErrorCode.CONTINUE_AUCTION);
        }

        Member buyer = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        if (buyer.getPoint() < confirmDto.getPrice()) { // 회원의 포인트가 부족한 경우
            throw new AuctionException(AuctionErrorCode.FAIL_CONFIRM_AUCTION_BY_BUYER);
        }

        Member seller = memberRepository.findById(confirmDto.getSellerId())
            .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        seller = seller.toBuilder()
            .point(seller.getPoint() + confirmDto.getPrice()) // 판매자의 포인트 증가
            .build();
        memberRepository.save(seller);

        PointHistory buyerPointHistory = PointHistory.builder()
            .curPointAmount(buyer.getPoint())
            .pointType(PointType.USE)
            .pointAmount(confirmDto.getPrice())
            .member(buyer)
            .build();
        pointRepository.save(buyerPointHistory); // 구매자 포인트 히스토리 저장

        PointHistory sellerPointHistory = PointHistory.builder()
            .curPointAmount(seller.getPoint())
            .pointType(PointType.GET)
            .pointAmount(confirmDto.getPrice())
            .member(seller)
            .build();
        pointRepository.save(sellerPointHistory); // 판매자 포인트 히스토리 저장

        Transaction buyerTransaction = Transaction.builder()
            .auction(auction)
            .member(buyer)
            .price(confirmDto.getPrice())
            .transType(TransType.BUY)
            .build();
        transactionRepository.save(buyerTransaction); // 구매자 거래 내역 저장

        Transaction sellerTransaction = Transaction.builder()
            .auction(auction)
            .member(seller)
            .price(confirmDto.getPrice())
            .transType(TransType.SELL)
            .build();
        transactionRepository.save(sellerTransaction); // 판매자 거래 내역 저장
    }

    // 이미지 연관관계 경매와 함께 저장하기 위한 메소드
    private void addImageList(List<Image> imageList, Auction auction) {

        imageList.stream()
            .map(i -> i.toBuilder()
                .auction(auction)
                .build()
            ).forEach(auction::addImageList);
    }
}
