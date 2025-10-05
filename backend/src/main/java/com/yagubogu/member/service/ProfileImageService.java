package com.yagubogu.member.service;

import com.yagubogu.global.exception.PayloadTooLargeException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.PreSignedUrlCompleteRequest;
import com.yagubogu.member.dto.PreSignedUrlCompleteResponse;
import com.yagubogu.member.dto.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.PresignedUrlStartResponse;
import com.yagubogu.member.repository.MemberRepository;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProfileImageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String IMAGES_PROFILES_PREFIX = "yagubogu/images/profiles/";

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final MemberRepository memberRepository;

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
                .contentLength(preSignedUrlStartRequest.contentLength())
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putReq));

        return new PresignedUrlStartResponse(key, presigned.url().toString());
    }

    @Transactional
    public PreSignedUrlCompleteResponse completeUpload(
            final Long memberId,
            final PreSignedUrlCompleteRequest request
    ) {
        String key = request.key();

        String objectUrl = s3Client.utilities()
                .getUrl(b -> b.bucket(bucket).key(key))
                .toExternalForm();

        Member member = getMember(memberId);
        member.updateImageUrl(objectUrl);

        return new PreSignedUrlCompleteResponse(objectUrl);
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
    }

    private void validateContentLength(final PreSignedUrlStartRequest preSignedUrlStartRequest) {
        if (preSignedUrlStartRequest.contentLength() > MAX_FILE_SIZE) {
            throw new PayloadTooLargeException(
                    "Content length is too large: " + preSignedUrlStartRequest.contentLength()
            );
        }
    }
}
