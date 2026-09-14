package com.yagubogu.checkin.service;

import com.yagubogu.global.config.S3Properties;
import com.yagubogu.global.exception.PayloadTooLargeException;
import com.yagubogu.global.exception.UnsupportedMediaTypeException;
import com.yagubogu.member.dto.v1.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.v1.PresignedUrlStartResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.util.Set;
import java.util.UUID;

@Service
public class CheckInImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String PATH_PREFIX = "images/check-ins/";
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/jpg", "image/png");
    private final S3Properties s3Properties;
    private final S3Presigner s3Presigner;

    public CheckInImageService(
            final S3Properties s3Properties,
            @Qualifier("checkInPresigner") final S3Presigner s3Presigner
    ) {
        this.s3Properties = s3Properties;
        this.s3Presigner = s3Presigner;
    }

    public PresignedUrlStartResponse issuePresignedUrl(PreSignedUrlStartRequest request) {
        if (request.contentLength() > MAX_FILE_SIZE) {
            throw new PayloadTooLargeException("Check-in image is too large");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
            throw new UnsupportedMediaTypeException("Check-in image content type not supported");
        }

        String key = PATH_PREFIX + UUID.randomUUID();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.checkInPrivateBucket())
                .key(key)
                .contentType(request.contentType())
                .contentLength(request.contentLength())
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(b -> b
                .signatureDuration(s3Properties.presignExpiration())
                .putObjectRequest(putObjectRequest));

        return new PresignedUrlStartResponse(key, presignedPutObjectRequest.url().toString());
    }
}
