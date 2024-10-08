package com.ddang.usedauction.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 트랜잭션 분리를 위한 클래스
@Component
public class AopForTransaction {

    @Transactional
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {

        return joinPoint.proceed();
    }
}
