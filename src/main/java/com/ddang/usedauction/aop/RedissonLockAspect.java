package com.ddang.usedauction.aop;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.ddang.usedauction.aop.RedissonLock)")
    public void redissonLock(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 현재 실행 중인 메서드의 시그니처
        Method method = signature.getMethod(); // 메소드 객체를 가져옴
        RedissonLock annotation = method.getAnnotation(
            RedissonLock.class); // RedissonLock 어노테이션을 가져옴
        String lockKey = parseSpel(annotation.value(), joinPoint);
        log.info("lockKey = {}", lockKey);

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean lockable = lock.tryLock(annotation.waitTime(), annotation.leaseTime(),
                TimeUnit.MILLISECONDS); // 최대 waitTime 동안 락을 얻기 위해 시도, leaseTime 동안 락 사용
            if (!lockable) {
                log.error("Lock 획득 실패 = {}", lockKey);
                return;
            }

            log.info("Lock 얻고 로직 실행");
            aopForTransaction.proceed(joinPoint); // 어노테이션이 선언된 메소드를 별도의 트랜잭션으로 실행
        } catch (InterruptedException e) {
            log.error("Lock 에러 발생", e);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    // 전달받은 Lock의 이름을 Spring Expression Language 로 파싱
    private String parseSpel(String spel, ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();

        for (int i = 0; i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        return parser.parseExpression(spel).getValue(context, String.class);
    }
}
