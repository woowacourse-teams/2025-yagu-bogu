package com.yagubogu.checkin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yagubogu.global.config.S3Properties;
import com.yagubogu.global.exception.PayloadTooLargeException;
import com.yagubogu.global.exception.UnsupportedMediaTypeException;
import com.yagubogu.member.dto.v1.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.v1.PresignedUrlStartResponse;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class CheckInImageServiceTest {

    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_CHECK_IN_PRIVATE_BUCKET = "test-check-in-private-bucket";
    private static final String TEST_ENDPOINT = "https://test-endpoint.com";

    @Mock
    private S3Presigner s3Presigner;
    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    private CheckInImageService checkInImageService;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties(
                TEST_BUCKET,
                Duration.ofMinutes(10),
                TEST_ENDPOINT,
                "ap-chuncheon-1",
                TEST_ENDPOINT + "/" + TEST_BUCKET,
                "http://default.img",
                TEST_CHECK_IN_PRIVATE_BUCKET,
                "test-check-in-access-key",
                "test-check-in-secret-key"
        );
        checkInImageService = new CheckInImageService(s3Properties, s3Presigner);
    }

    @DisplayName("pre-signed url을 발급한다")
    @Test
    void issuePresignedUrl_success() throws MalformedURLException {
        // given
        PreSignedUrlStartRequest request = new PreSignedUrlStartRequest("image/jpeg", 1_000_000L);
        String fakeUrl = "https://test-bucket.s3.amazonaws.com/images/check-ins/some-uuid";
        when(presignedPutObjectRequest.url()).thenReturn(new URL(fakeUrl));
        when(s3Presigner.presignPutObject(any(Consumer.class))).thenReturn(presignedPutObjectRequest);

        // when
        PresignedUrlStartResponse response = checkInImageService.issuePresignedUrl(request);

        // then
        ArgumentCaptor<Consumer<PutObjectPresignRequest.Builder>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(s3Presigner).presignPutObject(captor.capture());

        PutObjectPresignRequest.Builder builder = PutObjectPresignRequest.builder();
        captor.getValue().accept(builder);
        PutObjectRequest capturedRequest = builder.build().putObjectRequest();

        assertThat(response.key()).startsWith("images/check-ins/");
        assertThat(response.url()).isEqualTo(fakeUrl);
        assertThat(capturedRequest.bucket()).isEqualTo(TEST_CHECK_IN_PRIVATE_BUCKET);
    }

    @DisplayName("예외: contentLength가 최대 길이를 초과하면 예외를 던진다")
    @Test
    void issuePresignedUrl_tooLarge() {
        // given
        PreSignedUrlStartRequest request = new PreSignedUrlStartRequest("image/jpeg", 5L * 1024 * 1024 + 1L);

        // when & then
        assertThatThrownBy(() -> checkInImageService.issuePresignedUrl(request))
                .isInstanceOf(PayloadTooLargeException.class)
                .hasMessageContaining("Check-in image is too large");
    }

    @DisplayName("예외: 허용되지 않은 contentType이면 예외를 던진다")
    @Test
    void issuePresignedUrl_invalidContentType() {
        // given
        PreSignedUrlStartRequest request = new PreSignedUrlStartRequest("image/gif", 1_000_000L);

        // when & then
        assertThatThrownBy(() -> checkInImageService.issuePresignedUrl(request))
                .isInstanceOf(UnsupportedMediaTypeException.class)
                .hasMessageContaining("Check-in image content type not supported");
    }
}
