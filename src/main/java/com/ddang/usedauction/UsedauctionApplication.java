package com.ddang.usedauction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableCaching // 캐시 기능 활성화
@EnableAspectJAutoProxy // aop 사용
public class UsedauctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsedauctionApplication.class, args);
    }

}
