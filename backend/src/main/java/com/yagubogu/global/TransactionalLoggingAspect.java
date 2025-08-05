package com.yagubogu.global;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Aspect
public class TransactionalLoggingAspect {

    @Around("@annotation(transactional)")
    public Object logWritableTransactions(
            final ProceedingJoinPoint joinPoint,
            final Transactional transactional
    ) throws Throwable {
        String traceId = UUID.randomUUID().toString().substring(0, 12);
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        long startTime = System.currentTimeMillis();
        if (!transactional.readOnly()) {
            log.info("[{}] [{}] {} - 트랜잭션 시작: {}",
                    traceId,
                    className + "." + methodName,
                    className,
                    args);
        }

        try {
            Object result = joinPoint.proceed();

            return result;
        } finally {
            if (!transactional.readOnly()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                log.info("[{}] [{}] {} - 트랜잭션 완료 ({}ms)",
                        traceId,
                        className + "." + methodName,
                        className,
                        elapsedTime);
            }
        }
    }
}
