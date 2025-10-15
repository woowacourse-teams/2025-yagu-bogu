package com.yagubogu.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.doReturn;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.config.S3Properties;
import com.yagubogu.global.exception.PayloadTooLargeException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.PreSignedUrlCompleteRequest;
import com.yagubogu.member.dto.PreSignedUrlCompleteResponse;
import com.yagubogu.member.dto.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.PresignedUrlStartResponse;
import com.yagubogu.support.member.MemberFactory;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class ProfileImageServiceTest {

    private static final String TEST_BUCKET = "test-bucket";
    private static final Duration TEST_PRESIGN_EXPIRATION = Duration.ofMinutes(10);

    @Autowired
    MemberService memberService;

    @Autowired
    private MemberFactory memberFactory;

    private ProfileImageService profileImageService;
    private S3Presigner s3Presigner;
    private S3Client s3Client;
    private S3Properties s3Properties;

    @BeforeEach
    void setUp() {
        StaticCredentialsProvider creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test", "test")
        );

        s3Presigner = S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(creds)
                .build();

        S3Client realS3Client = S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(creds)
                .build();

        s3Client = org.mockito.Mockito.spy(realS3Client);
        doReturn(software.amazon.awssdk.services.s3.model.HeadObjectResponse.builder().build())
                .when(s3Client).headObject(org.mockito.ArgumentMatchers.any(HeadObjectRequest.class));

        s3Properties = new S3Properties(TEST_BUCKET, TEST_PRESIGN_EXPIRATION);

        profileImageService = new ProfileImageService(s3Presigner, s3Client, s3Properties, memberService);
    }

    @Test
    @DisplayName("pre-signed url을발급한다")
    void issuePreSignedUrl_success() throws Exception {
        // given
        PreSignedUrlStartRequest request = new PreSignedUrlStartRequest("image/jpeg", 1_000_000L);

        // when
        PresignedUrlStartResponse response = profileImageService.issuePreSignedUrl(request);

        // then
        URL url = new URL(response.url());
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.key()).startsWith("yagubogu/images/profiles/");
            softAssertions.assertThat(url.getHost()).contains(TEST_BUCKET);
            softAssertions.assertThat(response.url()).contains(response.key());
        });
    }

    @Test
    @DisplayName("contentLength가 최대 길이를 초과하면 예외를 던진다")
    void issuePreSignedUrl_tooLarge() {
        // given
        long tooLargeContentLength = 5L * 1024 * 1024 + 1L;
        PreSignedUrlStartRequest request = new PreSignedUrlStartRequest("image/jpeg", tooLargeContentLength);

        // when & then
        assertThatThrownBy(() -> profileImageService.issuePreSignedUrl(request))
                .isInstanceOf(PayloadTooLargeException.class)
                .hasMessageContaining("Content length is too large");
    }

    @Test
    @DisplayName("s3에 업로드된 이미지로 회원의 프로필 이미지 주소를 수정한다")
    void completeUpload_success() {
        // given
        String key = "yagubogu/images/profiles/abc-123";
        PreSignedUrlCompleteRequest request = new PreSignedUrlCompleteRequest(key);
        Member member = memberFactory.save(builder -> builder.build());

        S3Utilities utilities = s3Client.utilities();
        String expectedUrl = utilities.getUrl(b -> b.bucket(TEST_BUCKET).key(key)).toExternalForm();

        // when
        PreSignedUrlCompleteResponse response = profileImageService.completeUpload(member.getId(), request);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.url()).isEqualTo(expectedUrl);
            softAssertions.assertThat(member.getImageUrl()).isEqualTo(expectedUrl);
        });
    }
}
