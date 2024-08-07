package com.ddang.usedauction.auction.service;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.dto.AuctionServiceDto;
import com.ddang.usedauction.auction.exception.AuctionErrorCode;
import com.ddang.usedauction.auction.exception.AuctionException;
import com.ddang.usedauction.auction.repository.AuctionRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

        if (sorted.equals(VIEW)) { // 경메에 참여한 회원순으로 정렬해야하는 경우
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

    // 이미지 연관관계 경매와 함께 저장하기 위한 메소드
    private void addImageList(List<Image> imageList, Auction auction) {

        imageList.stream()
            .map(i -> i.toBuilder()
                .auction(auction)
                .build()
            ).forEach(auction::addImageList);
    }
}
