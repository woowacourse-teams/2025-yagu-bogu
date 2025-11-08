package com.yagubogu.sse;

import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SseKpis {

    public static final AtomicLong EVENTS = new AtomicLong(0);
    public static final AtomicLong QUERIES = new AtomicLong(0);
    public static final AtomicLong SEND_OK = new AtomicLong(0);

    public static void logEvent(long eventSeq, boolean cacheHit, long subscribers, long sendOk) {
        log.info("METRIC_EVENT {\"event\":{},\"cache_hit\":{},\"subscribers\":{},\"send_ok\":{}}",
                eventSeq, cacheHit, subscribers, sendOk);
    }

    public static void logQuery(long querySeq) {
        log.info("METRIC_QUERY {\"query\":{}}", querySeq);
    }
}

