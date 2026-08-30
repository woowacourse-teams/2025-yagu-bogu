package com.yagubogu.global.config;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    private final S3Properties s3Properties;

    @Bean
    @Primary
    S3Client s3() {
        return defaultS3ClientBuilder().build();
    }

    @Bean
    @Qualifier("checkInS3")
    S3Client checkInS3() {
        return defaultS3ClientBuilder()
                .credentialsProvider(checkInCredentialsProvider())
                .build();
    }

    @Bean
    @Primary
    S3Presigner presigner() {
        return defaultS3PresignerBuilder().build();
    }

    @Bean
    @Qualifier("checkInPresigner")
    S3Presigner checkInPresigner() {
        return defaultS3PresignerBuilder()
                .credentialsProvider(checkInCredentialsProvider())
                .build();
    }

    private S3ClientBuilder defaultS3ClientBuilder() {
        return S3Client.builder()
                .endpointOverride(URI.create(s3Properties.apiEndpoint()))
                .region(Region.of(s3Properties.region()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());
    }

    private S3Presigner.Builder defaultS3PresignerBuilder() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(s3Properties.apiEndpoint()))
                .region(Region.of(s3Properties.region()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());
    }

    private StaticCredentialsProvider checkInCredentialsProvider() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                s3Properties.checkInAccessKeyId(),
                s3Properties.checkInSecretAccessKey()
        );
        return StaticCredentialsProvider.create(credentials);
    }
}
