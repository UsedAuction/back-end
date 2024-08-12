package com.ddang.usedauction.auction.service;

import com.ddang.usedauction.aop.RedissonLock;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    /**
     * 경매글 단건 조회
     *
     * @param auctionId 조회할 경매글 PK
     * @return 조회된 경매글 serviceDto
     */
    @Transactional(readOnly = true)
    public Auction getAuction(Long auctionId) {

        return auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NullPointerException("존재하지 않는 경매입니다."));
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
    public Page<Auction> getAuctionList(String word, String category, String sorted,
        Pageable pageable) {

        final String VIEW = "view";

        Page<Auction> auctionPageList = auctionRepository.findAllByOptions(word, category, sorted,
            pageable);

        if (sorted != null && sorted.equals(VIEW)) { // 경메에 참여한 회원순으로 정렬해야하는 경우
            List<Auction> auctionList = auctionPageList.stream()
                .sorted(
                    (o1, o2) -> Math.toIntExact(o2.getBidMemberCount() - o1.getBidMemberCount()))
                .toList();

            return new PageImpl<>(auctionList, pageable, auctionPageList.getTotalElements());
        }

        return auctionPageList;
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
    public Auction createAuction(MultipartFile thumbnail, List<MultipartFile> imageList,
        String memberId, AuctionCreateDto.Request createDto) {

        if (imageList != null && imageList.size() > 5) { // 썸네일 포함 6개 초과인 경우
            throw new ImageCountOutOfBoundsException(imageList.size() + 1);
        }

        if (createDto.getEndedAt()
            .isAfter(LocalDateTime.now().plusDays(7))) { // 경매 끝나는 날짜가 일주일 초과되는 경우
            throw new AuctionMaxDateOutOfBoundsException();
        }

        if (createDto.getInstantPrice()
            <= createDto.getStartPrice()) { // 즉시 구매가가 입찰 시작가보다 적거나 같은 경우
            throw new StartPriceOutOfBoundsException(createDto.getStartPrice(),
                createDto.getInstantPrice());
        }

        // 직거래가 가능한 경우이지만 직거래 장소가 없는 경우
        if (!createDto.getTransactionType().equals(TransactionType.DELIVERY)
            && !StringUtils.hasText(createDto.getContactPlace())) {
            throw new IllegalArgumentException("거래 장소를 입력해주세요.");
        }

        // 택배 거래가 가능하지만 택베비가 없는 경우
        if (!createDto.getDeliveryType().equals(DeliveryType.NO_DELIVERY) && !StringUtils.hasText(
            createDto.getDeliveryPrice())) {
            throw new IllegalArgumentException("택배비를 입력해주세요.");
        }

        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new NullPointerException("존재하지 않는 경매입니다."));

        Category parentCategory = categoryRepository.findById(createDto.getParentCategoryId())
            .orElseThrow(() -> new NullPointerException("존재하지 않는 카테고리입니다."));

        Category childCategory = categoryRepository.findById(createDto.getChildCategoryId())
            .orElseThrow(() -> new NullPointerException("존재하지 않는 카테고리입니다."));

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

        return auctionRepository.save(auction);
    }

    /**
     * 경매 종료 서비스
     *
     * @param auctionId 종료할 경매 PK
     */
    public Map<String, Long> endAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        if (auction.getAuctionState().equals(AuctionState.END)) { // 이미 종료된 경매인 경우
            throw new IllegalStateException("현재 경매가 아직 진행중입니다.");
        }

        List<Bid> bidList = auction.getBidList();
        Bid bid = bidList != null ? bidList.stream()
            .max(Comparator.comparing(Bid::getBidPrice))
            .orElse(null) : null;

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
    @RedissonLock("#memberId")
    public void confirmAuction(Long auctionId, String memberId,
        AuctionConfirmDto.Request confirmDto) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NullPointerException("존재하지 않는 경매입니다."));

        if (auction.getAuctionState().equals(AuctionState.CONTINUE)) { // 아직 진행중인 경매인 경우
            throw new IllegalStateException("진행 중인 경매에는 구매 확정을 할 수 없습니다.");
        }

        Member buyer = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new NullPointerException("존재하지 않는 회원입니다."));

        if (buyer.getPoint() < confirmDto.getPrice()) { // 회원의 포인트가 부족한 경우
            throw new MemberPointOutOfBoundsException(buyer.getPoint(), confirmDto.getPrice());
        }

        Member seller = memberRepository.findById(confirmDto.getSellerId())
            .orElseThrow(() -> new NullPointerException("존재하지 않는 회원입니다."));

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
