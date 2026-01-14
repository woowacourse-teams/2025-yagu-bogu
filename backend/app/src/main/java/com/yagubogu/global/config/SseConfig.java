package com.yagubogu.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(SseHeartbeatProperties.class)
@Configuration
public class SseConfig {
}
