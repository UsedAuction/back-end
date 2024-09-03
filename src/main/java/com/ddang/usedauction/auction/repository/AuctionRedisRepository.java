package com.ddang.usedauction.auction.repository;

import com.ddang.usedauction.auction.dto.AuctionConfirmDto;
import com.ddang.usedauction.auction.event.AuctionAutoConfirmEvent;
import com.ddang.usedauction.auction.event.AuctionEndEvent;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class AuctionRedisRepository {

    private final RedissonClient redissonClient;
    private final ApplicationEventPublisher publisher;

    @Value("${spring.data.redis.auction.expire-key}")
    private String auctionExpireKey;

    @Value("${spring.data.redis.confirm.expire-key}")
    private String confirmExpireKey;

    // 경매가 생성됐을 때 해당 경매가 종료되는 시점에 맞도록 expire 시간을 설정하여 redis에 저장
    // expire될 때 publisher를 통해 이벤트 발생
    public void saveAuctionWithExpire(Long auctionId, long second) {

        RBucket<Object> bucket = redissonClient.getBucket(auctionExpireKey + auctionId);
        bucket.set("", Duration.ofSeconds(second));
        bucket.addListener((ExpiredObjectListener) name -> {
            publisher.publishEvent(AuctionEndEvent.of(auctionId));
            log.info("종료된 경매 PK = {}", auctionId);
        });
    }

    // 일주일 후 만료가 되는 bucket을 생성하여 redis에 저장
    // 만료되었을 때 이벤트 발생
    public void saveAuctionAutoConfirm(Long auctionId, String buyerId,
        AuctionConfirmDto.Request confirmDto) {

        RBucket<Object> bucket = redissonClient.getBucket(confirmExpireKey + auctionId);
        bucket.set("", Duration.ofDays(7)); // 7일 후 만료
        bucket.addListener((ExpiredObjectListener) name -> {
            publisher.publishEvent(AuctionAutoConfirmEvent.of(auctionId, buyerId, confirmDto));
            log.info("자동 구매 확정 처리된 경매 PK = {}", auctionId);
        });
    }
}
