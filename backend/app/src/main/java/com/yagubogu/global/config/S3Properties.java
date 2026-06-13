package com.yagubogu.global.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record S3Properties(
        String bucket,
        Duration presignExpiration,
        String endpoint,
        String region,
        String publicBaseUrl,
        String defaultProfileImageUrl,
        String checkInPrivateBucket,
        String checkInAccessKeyId,
        String checkInSecretAccessKey
) {

    public String apiEndpoint() {
        return removeTrailingKnownBucketPath(endpoint);
    }

    public String objectUrl(final String key) {
        return trimTrailingSlash(resolvePublicBaseUrl()) + "/" + trimLeadingSlash(key);
    }

    private String resolvePublicBaseUrl() {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return removeDuplicateTrailingBucketPath(publicBaseUrl);
        }
        String trimmedEndpoint = trimTrailingSlash(endpoint);
        if (hasTrailingBucketPath(trimmedEndpoint, bucket)) {
            return trimmedEndpoint;
        }
        return trimmedEndpoint + "/" + trimSlashes(bucket);
    }

    private String removeTrailingKnownBucketPath(final String value) {
        String trimmedValue = trimTrailingSlash(value);
        if (hasTrailingBucketPath(trimmedValue, bucket)) {
            return trimmedValue.substring(0, trimmedValue.length() - bucketPath(bucket).length());
        }
        if (hasTrailingBucketPath(trimmedValue, checkInPrivateBucket)) {
            return trimmedValue.substring(0, trimmedValue.length() - bucketPath(checkInPrivateBucket).length());
        }
        return trimmedValue;
    }

    private String removeDuplicateTrailingBucketPath(final String value) {
        String trimmedValue = trimTrailingSlash(value);
        String bucketPath = bucketPath(bucket);
        String duplicateBucketPath = bucketPath + bucketPath;
        if (!trimmedValue.endsWith(duplicateBucketPath)) {
            return trimmedValue;
        }
        return trimmedValue.substring(0, trimmedValue.length() - bucketPath.length());
    }

    private boolean hasTrailingBucketPath(final String value, final String bucketName) {
        return value != null && bucketName != null && !bucketName.isBlank() && value.endsWith(bucketPath(bucketName));
    }

    private String bucketPath(final String bucketName) {
        return "/" + trimSlashes(bucketName);
    }

    private static String trimTrailingSlash(final String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private static String trimLeadingSlash(final String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int start = 0;
        while (start < value.length() && value.charAt(start) == '/') {
            start++;
        }
        return value.substring(start);
    }

    private static String trimSlashes(final String value) {
        return trimTrailingSlash(trimLeadingSlash(value));
    }
}
