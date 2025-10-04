package com.yagubogu.member.service;

import com.yagubogu.global.exception.PayloadTooLargeException;
import com.yagubogu.member.dto.PreSignedUrlCompleteRequest;
import com.yagubogu.member.dto.PreSignedUrlCompleteResponse;
import com.yagubogu.member.dto.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.PresignedUrlStartResponse;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    public static final String IMAGES_PROFILES_PREFIX = "yagubogu/images/profiles/";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.s3.bucket}")
    String bucket;

    public PresignedUrlStartResponse issuePreSignedUrl(PreSignedUrlStartRequest preSignedUrlStartRequest) {
        validateContentLength(preSignedUrlStartRequest);
        String uniqueFileName = UUID.randomUUID().toString();
        String key = IMAGES_PROFILES_PREFIX + uniqueFileName;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(preSignedUrlStartRequest.contentType())
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putReq));

        return new PresignedUrlStartResponse(key, presigned.url().toString());
    }

    public PreSignedUrlCompleteResponse completeUpload(final PreSignedUrlCompleteRequest request) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(request.key())
                .build();

        GetObjectPresignRequest presigner = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presigner);

        return new PreSignedUrlCompleteResponse(presigned.url().toString());
    }

    private void validateContentLength(final PreSignedUrlStartRequest preSignedUrlStartRequest) {
        if (preSignedUrlStartRequest.contentLength() > MAX_FILE_SIZE) {
            throw new PayloadTooLargeException(
                    "Content length is too large: " + preSignedUrlStartRequest.contentLength());
        }
    }
}
