package com.yagubogu.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {
    
    @Bean
    S3Client s3() {
        return S3Client.builder().region(Region.AP_NORTHEAST_2).build();
    }

    @Bean
    S3Presigner presigner() {
        return S3Presigner.builder().region(Region.AP_NORTHEAST_2).build();
    }
}
