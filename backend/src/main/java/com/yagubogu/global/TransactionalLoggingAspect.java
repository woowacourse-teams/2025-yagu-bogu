package com.yagubogu.global;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class TransactionalLoggingAspect {

    @Around("@within(org.springframework.stereotype.Service)")
    public Object logWritableTransactions(final ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        long startTime = System.currentTimeMillis();
        log.info("[{}] - [BEGIN TX]", className + "." + methodName);

        try {
            return joinPoint.proceed();
        } finally {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("[{}] - [END TX] ({}ms)", className + "." + methodName, elapsedTime);
        }
    }
}
