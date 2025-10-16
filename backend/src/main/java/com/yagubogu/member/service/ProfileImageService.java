package com.yagubogu.member.service;

import com.yagubogu.global.config.S3Properties;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.PayloadTooLargeException;
import com.yagubogu.global.exception.UnsupportedMediaTypeException;
import com.yagubogu.member.dto.PreSignedUrlCompleteRequest;
import com.yagubogu.member.dto.PreSignedUrlCompleteResponse;
import com.yagubogu.member.dto.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.PresignedUrlStartResponse;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@RequiredArgsConstructor
@Service
public class ProfileImageService {

    private static final long MAX_FILE_SIZE_MB = 5;
    private static final long MAX_FILE_SIZE = MAX_FILE_SIZE_MB * 1024 * 1024;
    private static final String IMAGES_PROFILES_PREFIX = "yagubogu/images/profiles/";
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg");

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final MemberService memberService;

    public PresignedUrlStartResponse issuePreSignedUrl(final PreSignedUrlStartRequest preSignedUrlStartRequest) {
        validateContentLength(preSignedUrlStartRequest);
        validateContentType(preSignedUrlStartRequest.contentType());

        String uniqueFileName = UUID.randomUUID().toString();
        String key = IMAGES_PROFILES_PREFIX + uniqueFileName;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .contentType(preSignedUrlStartRequest.contentType())
                .contentLength(preSignedUrlStartRequest.contentLength())
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(b -> b
                .signatureDuration(s3Properties.presignExpiration())
                .putObjectRequest(putReq));

        return new PresignedUrlStartResponse(key, presigned.url().toString());
    }

    public PreSignedUrlCompleteResponse completeUpload(
            final Long memberId,
            final PreSignedUrlCompleteRequest request
    ) {
        String key = request.key();
        assertObjectExists(key);

        String objectUrl = s3Client.utilities()
                .getUrl(b -> b.bucket(s3Properties.bucket()).key(key))
                .toExternalForm();

        return updateProfileImage(memberId, objectUrl);
    }

    private PreSignedUrlCompleteResponse updateProfileImage(Long memberId, String objectUrl) {
        memberService.updateProfileImageUrl(memberId, objectUrl);
        return new PreSignedUrlCompleteResponse(objectUrl);
    }

    private void assertObjectExists(final String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .build();
            s3Client.headObject(headObjectRequest);
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("File does not exist in S3: " + key);
        }
    }

    private void validateContentLength(final PreSignedUrlStartRequest preSignedUrlStartRequest) {
        if (preSignedUrlStartRequest.contentLength() > MAX_FILE_SIZE) {
            throw new PayloadTooLargeException(
                    "Content length is too large: " + preSignedUrlStartRequest.contentLength()
            );
        }
    }

    private void validateContentType(final String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new UnsupportedMediaTypeException(
                    "Content type is invalid: " + contentType
            );
        }
    }
}
