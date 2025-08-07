package com.yagubogu.global;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        MDC.clear();
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        MDC.put("traceId", traceId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("clientIp", request.getRemoteAddr());
        MDC.put("userAgent", shortenUserAgent(request.getHeader("User-Agent")));

        if (handler instanceof final HandlerMethod handlerMethod) {
            MDC.put("controller", handlerMethod.getBeanType().getSimpleName());
            MDC.put("action", handlerMethod.getMethod().getName());
        }
        log.info("Request is started");

        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        try {
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime == null) {
                return;
            }
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", duration + "ms");
            MDC.put("status", String.valueOf(response.getStatus()));

            if (ex != null) {
                MDC.put("exception", ex.getClass().getSimpleName());
                log.error("Request is failed", ex);
                return;
            }
            if (response.getStatus() >= HttpStatus.BAD_REQUEST.value()) {
                log.warn("Request error");
                return;
            }
            log.info("Request is completed ({}ms)", duration);
        } finally {
            MDC.clear();
        }
    }

    private String shortenUserAgent(String userAgent) {
        if (userAgent == null) {
            return "UNKNOWN";
        }
        return userAgent.length() > 50 ? userAgent.substring(0, 50) + "…" : userAgent;
    }
}
