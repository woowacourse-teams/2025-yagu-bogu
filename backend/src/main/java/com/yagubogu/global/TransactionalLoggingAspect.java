package com.yagubogu.global;

import com.yagubogu.global.exception.YaguBoguException;
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
        String signature = joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        log.info("[{}] - [BEGIN TX]", signature);

        try {
            Object proceed = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("[{}] - [END TX] ({}ms)", signature, elapsedTime);
            return proceed;
        } catch (YaguBoguException e) {
            throw e;
        } catch (Throwable t) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[{}] - [FAILED TX] ({}ms)", signature, elapsedTime);
            throw t;
        }
    }
}
