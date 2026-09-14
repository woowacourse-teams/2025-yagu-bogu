package com.yagubogu.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class S3PropertiesTest {

    @DisplayName("publicBaseUrl 기준으로 객체 URL을 만든다")
    @Test
    void objectUrl_usesPublicBaseUrl() {
        S3Properties properties = new S3Properties(
                "yagubogu",
                Duration.ofMinutes(5),
                "https://test-account.r2.cloudflarestorage.com",
                "auto",
                "https://images.yagubogu.com/",
                "https://images.yagubogu.com/images/defaults/profile.png",
                "yagubogu-check-in-image-private",
                "test-check-in-access-key",
                "test-check-in-secret-key"
        );

        String objectUrl = properties.objectUrl("/profile/abc-123");

        assertThat(objectUrl)
                .isEqualTo("https://images.yagubogu.com/profile/abc-123");
    }

    @DisplayName("publicBaseUrl이 없으면 endpoint와 bucket으로 객체 URL을 만든다")
    @Test
    void objectUrl_fallbackToEndpointAndBucket() {
        S3Properties properties = new S3Properties(
                "yagubogu",
                Duration.ofMinutes(5),
                "https://test-account.r2.cloudflarestorage.com/",
                "auto",
                null,
                "https://test-account.r2.cloudflarestorage.com/yagubogu/images/defaults/profile.png",
                "yagubogu-check-in-image-private",
                "test-check-in-access-key",
                "test-check-in-secret-key"
        );

        String objectUrl = properties.objectUrl("profile/abc-123");

        assertThat(objectUrl)
                .isEqualTo("https://test-account.r2.cloudflarestorage.com/yagubogu/profile/abc-123");
    }

    @DisplayName("endpoint에 bucket 경로가 있어도 API endpoint에서는 bucket 경로를 제거한다")
    @Test
    void apiEndpoint_removesBucketPath() {
        S3Properties properties = new S3Properties(
                "yagubogu",
                Duration.ofMinutes(5),
                "https://test-account.r2.cloudflarestorage.com/yagubogu",
                "auto",
                null,
                "https://test-account.r2.cloudflarestorage.com/yagubogu/images/defaults/profile.png",
                "yagubogu-check-in-image-private",
                "test-check-in-access-key",
                "test-check-in-secret-key"
        );

        assertThat(properties.apiEndpoint())
                .isEqualTo("https://test-account.r2.cloudflarestorage.com");
    }

    @DisplayName("publicBaseUrl에 bucket 경로가 중복되어도 객체 URL에서는 한 번만 사용한다")
    @Test
    void objectUrl_removesDuplicatedBucketPath() {
        S3Properties properties = new S3Properties(
                "yagubogu",
                Duration.ofMinutes(5),
                "https://test-account.r2.cloudflarestorage.com/yagubogu",
                "auto",
                "https://test-account.r2.cloudflarestorage.com/yagubogu/yagubogu",
                "https://test-account.r2.cloudflarestorage.com/yagubogu/images/defaults/profile.png",
                "yagubogu-check-in-image-private",
                "test-check-in-access-key",
                "test-check-in-secret-key"
        );

        String objectUrl = properties.objectUrl("profile/abc-123");

        assertThat(objectUrl)
                .isEqualTo("https://test-account.r2.cloudflarestorage.com/yagubogu/profile/abc-123");
    }

    @DisplayName("endpoint에 check-in private bucket 경로가 있어도 API endpoint에서는 bucket 경로를 제거한다")
    @Test
    void apiEndpoint_removesCheckInPrivateBucketPath() {
        S3Properties properties = new S3Properties(
                "yagubogu",
                Duration.ofMinutes(5),
                "https://test-account.r2.cloudflarestorage.com/yagubogu-check-in-image-private",
                "auto",
                null,
                "https://test-account.r2.cloudflarestorage.com/yagubogu/images/defaults/profile.png",
                "yagubogu-check-in-image-private",
                "test-check-in-access-key",
                "test-check-in-secret-key"
        );

        assertThat(properties.apiEndpoint())
                .isEqualTo("https://test-account.r2.cloudflarestorage.com");
    }
}
