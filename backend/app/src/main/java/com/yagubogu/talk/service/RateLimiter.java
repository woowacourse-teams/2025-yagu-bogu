package com.yagubogu.talk.service;

public interface RateLimiter {
    /**
     * Rate Limit 체크
     *
     * @param key 제한 대상 식별자 (예: "talk:member:5001")
     * @param maxRequests 최대 요청 수
     * @param windowSeconds 시간 윈도우 (초)
     * @throws RateLimitExceededException 제한 초과 시
     */
    void checkLimit(String key, int maxRequests, int windowSeconds);
}
