package com.ddang.usedauction.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonLock {

    String value(); // Lock의 이름 (고유값)

    long waitTime() default 2000L; // Lock 획득을 시도하는 최대 시간 (ms)

    long leaseTime() default 3000L; // 락을 획득한 후, 점유하는 최대 시간 (ms)
}
