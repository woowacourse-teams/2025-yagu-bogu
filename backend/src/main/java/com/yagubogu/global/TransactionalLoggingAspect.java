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

    private static final String LOG_FORMAT = "[{}] - [END TX] ({}ms)";

    @Around("@within(org.springframework.stereotype.Service) && execution(* com.yagubogu..*(..))")
    public Object logWritableTransactions(final ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        log.info("[{}] - [BEGIN TX]", signature);

        try {
            Object proceed = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info(LOG_FORMAT, signature, elapsedTime);
            return proceed;
        } catch (YaguBoguException e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info(LOG_FORMAT, signature, elapsedTime);
            throw e;
        } catch (Throwable t) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error(LOG_FORMAT, signature, elapsedTime);
            throw t;
        }
    }
}
