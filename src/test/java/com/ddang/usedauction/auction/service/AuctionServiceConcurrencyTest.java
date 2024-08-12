package com.ddang.usedauction.auction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.domain.DeliveryType;
import com.ddang.usedauction.auction.domain.TransactionType;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.category.domain.Category;
import com.ddang.usedauction.category.repository.CategoryRepository;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AuctionServiceConcurrencyTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuctionService auctionService;

    Long auctionId;

    @BeforeEach
    void setup() {

        for (long i = 1; i <= 30; i++) {
            Member member = Member.builder()
                .siteAlarm(false)
                .passWord("1234")
                .email("test@naver.com")
                .point(2000)
                .memberId("test" + i)
                .build();
            memberRepository.save(member);
        }

        Member seller = Member.builder()
            .siteAlarm(false)
            .memberId("seller")
            .point(0)
            .email("seller@naver.com")
            .passWord("1234")
            .build();
        Member savedSeller = memberRepository.save(seller);

        Category parentCategory = Category.builder()
            .categoryName("category1")
            .build();
        Category savedParentCategory = categoryRepository.save(parentCategory);

        Category childCategory = Category.builder()
            .categoryName("category2")
            .parentId(1L)
            .build();
        Category savedChildCategory = categoryRepository.save(childCategory);

        Auction auction = Auction.builder()
            .title("title")
            .contactPlace("place")
            .transactionType(TransactionType.CONTACT)
            .productStatus(3.5)
            .startPrice(1000)
            .productName("name")
            .productDescription("description")
            .productColor("color")
            .auctionState(AuctionState.CONTINUE)
            .deliveryType(DeliveryType.NO_DELIVERY)
            .endedAt(LocalDateTime.now().plusDays(2))
            .childCategory(savedChildCategory)
            .parentCategory(savedParentCategory)
            .seller(savedSeller)
            .currentPrice(1000)
            .build();
        Auction savedAuction = auctionRepository.save(auction);
        auctionId = savedAuction.getId();
    }

    @Test
    @DisplayName("즉시 구매 동시성 테스트")
    void instantPurchaseAuctionWithoutLock() throws InterruptedException {

        long memberCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool((int) memberCount);
        CountDownLatch latch = new CountDownLatch((int) memberCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        for (long i = 1; i <= memberCount; i++) {
            long finalI = i;
            executorService.submit(() -> {
                try {
                    auctionService.instantPurchaseAuction(auctionId, "test" + finalI);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assertEquals(1, successCount.get()); // 1개 성공
        assertEquals(memberCount - 1, failCount.get()); // 나머지 29개 실패
    }
}
