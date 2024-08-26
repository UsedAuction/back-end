package com.ddang.usedauction.auction.service;

import static com.ddang.usedauction.notification.domain.NotificationType.CONFIRM;
import static com.ddang.usedauction.notification.domain.NotificationType.DONE_INSTANT;

import com.ddang.usedauction.aop.RedissonLock;
import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.ReceiveType;
import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.dto.AuctionCreateDto.Request;
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
import com.ddang.usedauction.chat.service.ChatRoomService;
import com.ddang.usedauction.image.domain.Image;
import com.ddang.usedauction.image.service.ImageService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.service.NotificationService;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.repository.PointRepository;
import com.ddang.usedauction.point.type.PointType;
import com.ddang.usedauction.transaction.domain.BuyType;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final PointRepository pointRepository;
    private final ImageService imageService;
    private final AuctionRedisService auctionRedisService;
    private final NotificationService notificationService;
    private final ChatRoomService chatRoomService;
    private final RedisTemplate<String, AuctionRecentDto> redisTemplate;

    private static final String RECENTLY_AUCTION_LIST_REDIS_KEY_PREFIX = "recently::";

    /**
     * 경매글 단건 조회
     *
     * @param auctionId 조회할 경매글 PK
     * @return 조회된 경매글 serviceDto
     */
    @Transactional(readOnly = true)
    public Auction getAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        String key =
            RECENTLY_AUCTION_LIST_REDIS_KEY_PREFIX + "test@example.com"; // todo: 토큰을 통한 회원 이메일
        redisTemplate.opsForList()
            .leftPush(key, AuctionRecentDto.from(auction)); // 레디스에 저장
        redisTemplate.opsForList().trim(key, 0, 4); // 리스트 길이 5로 유지
        redisTemplate.expire(key, Duration.ofHours(12)); // 만료 시간 설정 (12시간)

        return auction;
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
     * 현재 경매에 참여한 인원이 가장 많은 5개의 경매 리스트 조회
     *
     * @return 조회된 경매 리스트
     */
    @Transactional(readOnly = true)
    public List<Auction> getTop5(String mainCategory, String subCategory) {

        return auctionRepository.findTop5(mainCategory, subCategory);
    }

    /**
     * 최근 본 경매 리스트 조회
     *
     * @return 최근 본 경매 리스트
     */
    public List<AuctionRecentDto> getAuctionRecentList() {

        String key =
            RECENTLY_AUCTION_LIST_REDIS_KEY_PREFIX + "test@example.com"; // todo: 토큰을 통한 회원 이메일
        return redisTemplate.opsForList().range(key, 0, 4);
    }

    /**
     * 경매글 생성 서비스
     *
     * @param thumbnail   대표 이미지
     * @param imageList   대표 이미지를 제외한 이미지 리스트
     * @param memberEmail 경매글 작성자
     * @param createDto   경매글 작성 정보
     * @return 작성된 경매글의 serviceDto
     */
    @Transactional
    public Auction createAuction(MultipartFile thumbnail, List<MultipartFile> imageList,
        String memberEmail, AuctionCreateDto.Request createDto) {

        createValidation(imageList, createDto);

        Member member = memberRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        Category parentCategory = categoryRepository.findById(createDto.getParentCategoryId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));

        Category childCategory = categoryRepository.findById(createDto.getChildCategoryId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 카테고리입니다."));

        Auction auction = buildAuction(createDto, member, parentCategory, childCategory);

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
    @Transactional
    public AuctionEndDto endAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        if (auction.getAuctionState().equals(AuctionState.END)) { // 이미 종료된 경매인 경우
            throw new IllegalStateException("현재 경매가 이미 종료되었습니다.");
        }

        Bid bid = getSuccessfulBid(auction); // 낙찰된 입찰

        auction = auction.toBuilder()
            .auctionState(AuctionState.END) // 경매 종료 처리
            .build();
        Auction savedAuction = auctionRepository.save(auction);

        return AuctionEndDto.from(savedAuction, bid);
    }

    /**
     * 구매 확정 서비스
     *
     * @param auctionId   경매글 PK
     * @param memberEmail 구매자 이메일
     * @param confirmDto  구매 확정 정보
     */
    @RedissonLock("#confirmDto.sellerId")
    public void confirmAuction(Long auctionId, String memberEmail,
        AuctionConfirmDto.Request confirmDto) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        if (auction.getAuctionState().equals(AuctionState.CONTINUE)) { // 아직 진행중인 경매인 경우
            throw new IllegalStateException("진행 중인 경매에는 구매 확정을 할 수 없습니다.");
        }

        Transaction transaction = transactionRepository.findByBuyerEmailAndAuctionId(memberEmail,
                auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 거래내역입니다."));

        // 이미 구매확정이 진행된 경우
        if (transaction.getTransType().equals(TransType.SUCCESS)) {
            throw new IllegalStateException("이미 종료된 거래입니다.");
        }

        Member buyer = memberRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        Member seller = memberRepository.findById(confirmDto.getSellerId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        seller = seller.toBuilder()
            .point(seller.getPoint() + confirmDto.getPrice()) // 판매자의 포인트 증가
            .build();
        memberRepository.save(seller);

        // 포인트 히스토리와 거래 내역 저장
        savePointAndTransaction(confirmDto, buyer, seller, transaction);

        // 구매 확정 알림 전송
        sendNotificationForConfirm(buyer, auction);
    }

    /**
     * 즉시 구매 서비스
     *
     * @param auctionId   즉시 구매할 경매글의 PK
     * @param memberEmail 구매자 이메일
     */
    @RedissonLock("#auctionId")
    public void instantPurchaseAuction(Long auctionId, String memberEmail) {

        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 경매입니다."));

        Member buyer = memberRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

        validationOfInstantPurchase(auction, buyer);

        auction = auction.toBuilder()
            .auctionState(AuctionState.END) // 경매 종료 처리
            .build();
        auctionRepository.save(auction);

        reducePointAndSaveTransaction(buyer, auction); // 구매자 포인트 감소 처리 및 거래 내역 저장

        auctionRedisService.createAutoConfirm(auctionId, buyer.getEmail(),
            auction.getInstantPrice(),
            auction.getSeller()
                .getId()); // 일주일 후 자동 구매 확정 되도록 설정
        // 알림 전송
        sendNotificationForInstant(auction, buyer);

        chatRoomService.createChatRoom(buyer.getId(), auction.getId());
    }

    // 즉시 구매 시 validation
    private void validationOfInstantPurchase(Auction auction, Member buyer) {
        // 판매자가 즉시 구매를 진행하고자 하는 경우
        if (auction.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalStateException("판매자가 직접 즉시 구매할 수 없습니다.");
        }

        if (auction.getAuctionState().equals(AuctionState.END)) { // 종료된 경매에 즉시 구매 요청인 경우
            throw new IllegalStateException("이미 종료된 경매입니다.");
        }

        if (buyer.getPoint() < auction.getInstantPrice()) { // 구매자의 포인트가 부족한 경우
            throw new MemberPointOutOfBoundsException(buyer.getPoint(), auction.getInstantPrice());
        }
    }

    // 구매자 포인트 감소 처리 및 구매 내역 저장 메소드
    private void reducePointAndSaveTransaction(Member buyer, Auction auction) {
        buyer = buyer.toBuilder()
            .point(buyer.getPoint() - auction.getInstantPrice()) // 즉시 구매 가격만큼 포인트 차감
            .build();
        memberRepository.save(buyer);

        Transaction transaction = Transaction.builder()
            .price(auction.getInstantPrice())
            .transType(TransType.CONTINUE)
            .buyType(BuyType.INSTANT)
            .buyer(buyer)
            .auction(auction)
            .build();
        transactionRepository.save(transaction);
    }

    // 이미지 연관관계 경매와 함께 저장하기 위한 메소드
    private void addImageList(List<Image> imageList, Auction auction) {

        imageList.stream()
            .map(i -> i.toBuilder()
                .auction(auction)
                .build()
            ).forEach(auction::addImageList);
    }

    // 경매 엔티티 빌드
    private Auction buildAuction(Request createDto, Member member, Category parentCategory,
        Category childCategory) {
        return Auction.builder()
            .title(createDto.getTitle())
            .auctionState(AuctionState.CONTINUE)
            .productName(createDto.getProductName())
            .productColor(createDto.getProductColor())
            .productStatus(createDto.getProductStatus())
            .productDescription(createDto.getProductDescription())
            .receiveType(createDto.getReceiveType())
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
    }

    // 생성 시 체크해야할 사항 validation
    private void createValidation(List<MultipartFile> imageList, Request createDto) {

        if (imageList != null && imageList.size() > 5) { // 썸네일 포함 6개 초과인 경우
            throw new ImageCountOutOfBoundsException(imageList.size() + 1);
        }

        if (createDto.getEndedAt()
            .isAfter(LocalDateTime.now().plusDays(7))) { // 경매 끝나는 날짜가 일주일 초과되는 경우
            throw new AuctionMaxDateOutOfBoundsException();
        }

        if (createDto.getEndedAt().isBefore(LocalDateTime.now())) { // 경매 끝나는 날짜가 현재 날짜보다 이전인 경우
            throw new IllegalArgumentException("경매가 끝나는 날짜가 현재 날짜보다 이전입니다.");
        }

        if (createDto.getInstantPrice()
            <= createDto.getStartPrice()) { // 즉시 구매가가 입찰 시작가보다 적거나 같은 경우
            throw new StartPriceOutOfBoundsException(createDto.getStartPrice(),
                createDto.getInstantPrice());
        }

        // 직거래가 가능한 경우이지만 직거래 장소가 없는 경우
        if (!createDto.getReceiveType().equals(ReceiveType.DELIVERY)
            && !StringUtils.hasText(createDto.getContactPlace())) {
            throw new IllegalArgumentException("거래 장소를 입력해주세요.");
        }

        // 택배 거래가 가능하지만 택베비가 없는 경우
        if (!createDto.getDeliveryType().equals(DeliveryType.NO_DELIVERY) && !StringUtils.hasText(
            createDto.getDeliveryPrice())) {
            throw new IllegalArgumentException("택배비를 입력해주세요.");
        }
    }

    // 낙찰된 입찰 조회 및 거래 내역 저장 메소드
    private Bid getSuccessfulBid(Auction auction) {

        List<Bid> bidList = auction.getBidList();
        Bid bid = bidList != null ? bidList.stream()
            .max(Comparator.comparing(Bid::getBidPrice))
            .orElse(null) : null;

        if (bid != null) {
            Member member = bid.getMember();
            member = member.toBuilder()
                .point(member.getPoint() - bid.getBidPrice()) // 입찰자 포인트 차감
                .build();

            memberRepository.save(member);
        }

        Transaction transaction = Transaction.builder()
            .auction(auction)
            .buyer(null)
            .transType(TransType.NONE)
            .buyType(BuyType.NO_BUY)
            .price(0)
            .build();

        if (bid != null) {
            transaction = transaction.toBuilder()
                .buyType(BuyType.SUCCESSFUL_BID)
                .buyer(bid.getMember())
                .transType(TransType.CONTINUE)
                .price(bid.getBidPrice())
                .build();
        }
        transactionRepository.save(transaction); // 거래 내역 저장

        return bid;
    }

    // 포인트 히스토리와 거래 내역 저장 메소드
    private void savePointAndTransaction(AuctionConfirmDto.Request confirmDto, Member buyer,
        Member seller,
        Transaction transaction) {

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

        transaction = transaction.toBuilder()
            .transType(TransType.SUCCESS)
            .build();
        transactionRepository.save(transaction);
    }

    // 즉시구매시 알림 전송
    private void sendNotificationForInstant(Auction auction, Member buyer) {

        notificationService.send(
            auction.getSeller().getId(),
            auction.getId(),
            buyer.getMemberId() + "님의 즉시구매로 " + auction.getTitle() + " 경매가 종료되었습니다.",
            DONE_INSTANT
        );

        notificationService.send(
            buyer.getId(),
            auction.getId(),
            "즉시구매를 하여 " + auction.getTitle() + " 경매가 종료되었습니다.",
            DONE_INSTANT
        );
    }

    // 구매 확정 알림 전송
    private void sendNotificationForConfirm(Member buyer, Auction auction) {

        // 판매자
        notificationService.send(
            auction.getSeller().getId(),
            auction.getId(),
            buyer.getMemberId() + "님이 " + auction.getTitle() + " 경매의 구매를 확정했습니다.",
            CONFIRM
        );

        // 구매자
        notificationService.send(
            buyer.getId(),
            auction.getId(),
            auction.getTitle() + " 경매의 구매를 확정했습니다.",
            CONFIRM
        );
    }
}
