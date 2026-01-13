package com.yagubogu.global;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        MDC.clear();
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        MDC.put("traceId", traceId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        MDC.put("clientIp", request.getRemoteAddr());

        String params = getParameterString(request);
        MDC.put("params", params);

        if (handler instanceof final HandlerMethod handlerMethod) {
            MDC.put("controller", handlerMethod.getBeanType().getSimpleName());
            MDC.put("action", handlerMethod.getMethod().getName());
        }
        log.info("Request is started");

        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    private String getParameterString(final HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        paramMap.forEach((key, values) -> {
            String joinedValues = String.join(",", values);
            sb.append(key).append("=").append(joinedValues).append("&");
        });

        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
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

            if (response.getStatus() >= 500) {
                if (ex != null) {
                    MDC.put("exception", ex.getClass().getSimpleName());
                }
                log.error("Internal server error while processing request (5xx) ({}ms)", duration);
                return;
            }
            if (response.getStatus() >= 400) {
                log.info("Request client error (4xx) ({}ms)", duration);
                return;
            }
            log.info("Request is completed ({}ms)", duration);
        } finally {
            MDC.clear();
        }
    }
}
