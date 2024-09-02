package com.ddang.usedauction.config;

import static com.ddang.usedauction.notification.domain.NotificationType.DONE;

import com.ddang.usedauction.auction.domain.Auction;
import com.ddang.usedauction.auction.domain.AuctionState;
import com.ddang.usedauction.auction.dto.AuctionEndDto;
import com.ddang.usedauction.auction.repository.AuctionRepository;
import com.ddang.usedauction.auction.service.AuctionRedisService;
import com.ddang.usedauction.bid.domain.Bid;
import com.ddang.usedauction.chat.service.ChatRoomService;
import com.ddang.usedauction.member.domain.Member;
import com.ddang.usedauction.member.repository.MemberRepository;
import com.ddang.usedauction.notification.service.NotificationService;
import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.domain.PointType;
import com.ddang.usedauction.point.repository.PointRepository;
import com.ddang.usedauction.transaction.domain.BuyType;
import com.ddang.usedauction.transaction.domain.TransType;
import com.ddang.usedauction.transaction.domain.Transaction;
import com.ddang.usedauction.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final AuctionRepository auctionRepository;
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final PointRepository pointRepository;
    private final AuctionRedisService auctionRedisService;
    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;

    @Value("${job.job-name}")
    private String jobName;

    @Value("${job.step-name}")
    private String stepName;

    @Bean
    public Job auctionJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {

        return new JobBuilder(jobName, jobRepository)
            .incrementer(new RunIdIncrementer())
            .flow(auctionStep(jobRepository, transactionManager))
            .end()
            .build();
    }

    @Bean
    @JobScope
    public Step auctionStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {

        return new StepBuilder(stepName, jobRepository)
            .<Auction, Auction>chunk(10, transactionManager) // 데이터 10개씩 가져옴
            .reader(auctionItemReader()) // 데이터 가져오는 역할
            .processor(auctionItemProcessor()) // 데이터 처리 역할
            .writer(auctionItemWriter()) // 데이터 저장 또는 수정 또는 삭제 역할
            .listener(stepExecutionListener()) // 리스너, 해당 스텝에선 step 실행 전 엔티티 존재 여부 확인하여 실행할지 말지 결정
            .faultTolerant() // job 실패 시
            .retry(Exception.class) // 해당 에러 발생한 시점에 대해 재시도
            .retryLimit(2) // 재시도 2번만 진행
            .build();
    }

    // step 실행 전 엔티티 존재 확인 후 존재하는 경우에만 실행
    @Bean
    public StepExecutionListener stepExecutionListener() {

        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                long count = auctionRepository.count(); // 경매 엔티티 갯수
                if (count == 0) {
                    log.info("경매 엔티티가 없으므로 job 을 진행하지 않습니다.");
                    stepExecution.setExitStatus(ExitStatus.COMPLETED);
                }
            }
        };
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Auction> auctionItemReader() {

        return new JpaPagingItemReaderBuilder<Auction>()
            .pageSize(10)
            .queryString("select a from Auction a where a.auctionState = CONTINUE order by id asc")
            .entityManagerFactory(entityManagerFactory)
            .name("jpaPagingItemReader")
            .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Auction, Auction> auctionItemProcessor() {

        return auction -> {
            LocalDateTime curEndedAt = auction.getEndedAt();

            if (curEndedAt.isBefore(LocalDateTime.now())) {
                log.info("경매 id {} = 경매 종료 진행", auction.getId());
                auction = auction.toBuilder()
                    .auctionState(AuctionState.END)
                    .build();

                log.info("낙찰자 확인 진행");
                Bid bid = getSuccessfulBid(auction);
                log.info("낙찰자 = {}", bid != null ? bid.getMember().getMemberId() : null);

                AuctionEndDto auctionEndDto = AuctionEndDto.from(auction, bid);

                Long sellerId = auctionEndDto.getSellerId(); // 판매자 PK
                Long buyerId = auctionEndDto.getBuyerId(); // 입찰자 PK, null인 경우 없음
                long price = auctionEndDto.getPrice(); // 판매한 가격

                if (buyerId != null) { // 낙찰자가 있는 경우
                    log.info("낙찰자가 존재하여 구매자에게 알림 전송");
                    Member buyer = memberRepository.findById(buyerId)
                        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 회원입니다."));

                    // 일주일 후 자동 구매 확정 되도록 설정
                    auctionRedisService.createAutoConfirm(auction.getId(), buyer.getMemberId(),
                        price, sellerId);

                    // 구매자에게 경매 종료 알림보내기
                    sendNotificationForEnd(buyerId, auction);

                    chatRoomService.createChatRoom(buyer.getId(), auction.getId());
                }

                // 판매자에게 경매 종료 알림보내기
                sendNotificationForEnd(sellerId, auction);

                return auction;
            }

            log.info("경매가 종료되지 않았으므로 다시 redis 에 경매 자동 종료 시간 설정 후 저장");
            auctionRedisService.createWithExpire(auction, getExpireSecond(auction));

            return auction;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Auction> auctionItemWriter() {

        log.info("경매 변경된 내용 저장 진행");

        return auctionRepository::saveAll;
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

            PointHistory pointHistory = PointHistory.builder()
                .pointType(PointType.USE)
                .pointAmount(bid.getBidPrice())
                .curPointAmount(member.getPoint())
                .member(member)
                .build();
            pointRepository.save(pointHistory);
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

    // 경매 종료 알림 전송
    private void sendNotificationForEnd(Long memberId, Auction auction) {

        notificationService.send(
            memberId,
            auction.getId(),
            auction.getTitle() + " 경매가 종료되었습니다.",
            DONE
        );
    }

    // 경매의 종료 기간(만료 시간)을 초 단위로 계산하여 반환하는 메서드
    // auction의 생성 시간(createdAt)과 종료 시간(endedAt) 간의 차이를 구함
    private long getExpireSecond(Auction auction) {

        return Duration.between(
            auction.getCreatedAt(),
            auction.getEndedAt()
        ).getSeconds();
    }
}
