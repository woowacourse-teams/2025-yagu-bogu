package com.yagubogu.member.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.config.S3Properties;
import com.yagubogu.global.exception.PayloadTooLargeException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.v1.PreSignedUrlCompleteRequest;
import com.yagubogu.member.dto.v1.PreSignedUrlCompleteResponse;
import com.yagubogu.member.dto.v1.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.v1.PresignedUrlStartResponse;
import com.yagubogu.support.member.MemberFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.Builder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@ExtendWith(MockitoExtension.class)
@DataJpaTest
class ProfileImageServiceTest {

    private static final String TEST_BUCKET = "test-bucket";
    private static final Duration TEST_PRESIGN_EXPIRATION = Duration.ofMinutes(10);

    @Mock
    private MemberService memberService;
    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Utilities s3Utilities;
    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    @Autowired
    private MemberFactory memberFactory;

    private ProfileImageService profileImageService;
    private S3Properties s3Properties;

    @BeforeEach
    void setUp() {
        s3Properties = new S3Properties(TEST_BUCKET, TEST_PRESIGN_EXPIRATION);
        profileImageService = new ProfileImageService(s3Presigner, s3Client, s3Properties, memberService);
    }


    @DisplayName("pre-signed url을 발급한다")
    @Test
    void issuePreSignedUrl_success() throws MalformedURLException {
        // given
        PreSignedUrlStartRequest request = new PreSignedUrlStartRequest("image/jpeg", 1_000_000L);
        String fakeKeyPrefix = "yagubogu/images/profiles/";
        String fakeUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/" + fakeKeyPrefix + "some-uuid";
        when(presignedPutObjectRequest.url()).thenReturn(new URL(fakeUrl));
        when(s3Presigner.presignPutObject(any(Consumer.class))).thenReturn(
                presignedPutObjectRequest);

        // when
        PresignedUrlStartResponse response = profileImageService.issuePreSignedUrl(request);

        // then
        ArgumentCaptor<Consumer<Builder>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(s3Presigner).presignPutObject(captor.capture());

        PutObjectPresignRequest.Builder builder = PutObjectPresignRequest.builder();
        captor.getValue().accept(builder);
        PutObjectRequest capturedRequest = builder.build().putObjectRequest();

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.key()).startsWith(fakeKeyPrefix);
            softAssertions.assertThat(response.url()).isEqualTo(fakeUrl);
            softAssertions.assertThat(capturedRequest.bucket()).isEqualTo(TEST_BUCKET);
            softAssertions.assertThat(capturedRequest.contentType()).isEqualTo("image/jpeg");
            softAssertions.assertThat(capturedRequest.contentLength()).isEqualTo(1_000_000L);
        });
    }


    @DisplayName("예외: contentLength가 최대 길이를 초과하면 예외를 던진다")
    @Test
    void issuePreSignedUrl_tooLarge() {
        // given
        long tooLargeContentLength = 5L * 1024 * 1024 + 1L;
        PreSignedUrlStartRequest request = new PreSignedUrlStartRequest("image/jpeg", tooLargeContentLength);

        // when & then
        assertThatThrownBy(() -> profileImageService.issuePreSignedUrl(request))
                .isInstanceOf(PayloadTooLargeException.class)
                .hasMessageContaining("Content length is too large");

        verify(s3Presigner, never()).presignPutObject(any(java.util.function.Consumer.class));
    }

    @DisplayName("s3에 업로드된 이미지로 회원의 프로필 이미지 주소를 수정한다")
    @Test
    void completeUpload_success() throws MalformedURLException {
        // given
        String key = "yagubogu/images/profiles/abc-123";
        PreSignedUrlCompleteRequest request = new PreSignedUrlCompleteRequest(key);
        Member member = memberFactory.save(builder -> builder.build());

        String expectedUrl = "https://s3.amazonaws.com/" + TEST_BUCKET + "/" + key;

        // Mock 객체 행동 정의 (Stubbing)
        // 1. s3Client.headObject가 정상 응답을 반환하도록 설정
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
        // 2. s3Client.utilities()가 mock s3Utilities를 반환하도록 설정
        when(s3Client.utilities()).thenReturn(s3Utilities);
        // 3. s3Utilities.getUrl()이 예상 URL을 반환하도록 설정
        when(s3Utilities.getUrl(any(Consumer.class))).thenReturn(new URL(expectedUrl));

        // 4. memberService.updateProfileImageUrl이 호출되었을 때, member 객체의 imageUrl을 직접 수정하도록 설정
        doAnswer(invocation -> {
            Long memberId = invocation.getArgument(0);
            String imageUrl = invocation.getArgument(1);
            if (member.getId().equals(memberId)) {
                member.updateImageUrl(imageUrl);
            }
            return null;
        }).when(memberService).updateProfileImageUrl(anyLong(), anyString());

        // when
        PreSignedUrlCompleteResponse response = profileImageService.completeUpload(member.getId(), request);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.url()).isEqualTo(expectedUrl);
            softAssertions.assertThat(member.getImageUrl()).isEqualTo(expectedUrl);
        });

        // memberService의 updateProfileImageUrl 메서드가 정확한 인자와 함께 1번 호출되었는지 검증
        verify(memberService, times(1)).updateProfileImageUrl(member.getId(), expectedUrl);
    }
}
