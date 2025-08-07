package com.yagubogu.global;

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
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        long startTime = System.currentTimeMillis();
        if (!transactional.readOnly()) {
            log.info("[{}] - [BEGIN TX]",
                    className + "." + methodName);
        }

        try {
            Object result = joinPoint.proceed();

            return result;
        } finally {
            if (!transactional.readOnly()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                log.info("[{}] - [END TX] ({}ms)",
                        className + "." + methodName,
                        elapsedTime);
            }
        }
    }
}
