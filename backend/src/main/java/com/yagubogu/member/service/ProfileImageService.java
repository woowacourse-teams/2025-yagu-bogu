package com.yagubogu.member.service;

import com.yagubogu.global.exception.PayloadTooLargeException;
import com.yagubogu.member.dto.PreSignedUrlRequest;
import com.yagubogu.member.dto.PresignedUrlResponse;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private final S3Client s3Client;
    private final S3Presigner presigner;
    @Value("${app.s3.bucket}")
    String bucket;


    public PresignedUrlResponse issuePreSignedUrl(PreSignedUrlRequest preSignedUrlRequest) {
        validateContentLength(preSignedUrlRequest);
        String uniqueFileName = UUID.randomUUID().toString();
        String key = "images/profile/" + uniqueFileName;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(preSignedUrlRequest.contentType())
                .build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putReq));

        return new PresignedUrlResponse(key, presigned.url().toString());
    }

    private void validateContentLength(final PreSignedUrlRequest preSignedUrlRequest) {
        if (preSignedUrlRequest.contentLength() > MAX_FILE_SIZE) {
            throw new PayloadTooLargeException("Content length is too large: " + preSignedUrlRequest.contentLength());
        }
    }
}
