package com.yagubogu.reward.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 기프티콘 대사 설정을 애플리케이션에 등록한다.
 */
@EnableConfigurationProperties(GifticonReconciliationProperties.class)
@Configuration
public class GifticonReconciliationConfig {
}
