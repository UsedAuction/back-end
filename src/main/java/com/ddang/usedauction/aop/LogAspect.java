package com.ddang.usedauction.aop;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LogAspect {

    // 모든 메소드 포인트컷
    @Pointcut("execution(* com.ddang.usedauction..*(..)) "
        + "&& !execution(* com.ddang.usedauction.config..*(..)) "
        + "&& !execution(* com.ddang.usedauction.validation..*(..))")
    public void all() {
    }

    // Controller클래스의 모든 메소드 포인트컷
    @Pointcut("execution(* com.ddang.usedauction..*Controller.*(..))")
    public void controller() {
    }

    // Service클래스의 모든 메소드 포인트컷
    @Pointcut("execution(* com.ddang.usedauction..*Service.*(..))")
    public void service() {
    }

    // Service 또는 Controller의 메소드 실행 전 로그 표시
    @Before("controller() || service()")
    public void beforeLog(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.debug("[{}] {}메소드 실행", className, method.getName());

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                log.debug("type = {}", arg.getClass().getSimpleName());
                log.debug("value = {}", arg);
            }
        }
    }

    // 모든 메소드에서 예외 발생시 로그 표시
    @AfterThrowing(pointcut = "all()", throwing = "ex")
    public void afterThrowingLog(JoinPoint joinPoint, Throwable ex) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.error("[{}] {}메소드에서 예외 발생", className, method.getName());
        log.error("Exception message = {}", ex.getMessage());
    }
}
