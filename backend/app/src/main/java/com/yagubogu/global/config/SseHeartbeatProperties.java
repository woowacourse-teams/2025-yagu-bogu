package com.yagubogu.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sse.heartbeat")
public record SseHeartbeatProperties(boolean enabled, long intervalMs) {
}
