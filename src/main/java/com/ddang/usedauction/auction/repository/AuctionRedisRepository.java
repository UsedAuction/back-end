package com.ddang.usedauction.auction.repository;

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
}
