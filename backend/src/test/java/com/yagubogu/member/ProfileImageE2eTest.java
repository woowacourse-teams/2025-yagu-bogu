package com.yagubogu.member;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.doReturn;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.config.S3Properties;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.dto.PreSignedUrlCompleteRequest;
import com.yagubogu.member.dto.PreSignedUrlCompleteResponse;
import com.yagubogu.member.dto.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.PresignedUrlStartResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.base.E2eTestBase;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class ProfileImageE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private AuthFactory authFactory;

    @Autowired
    private S3Properties s3Properties;

    @MockitoSpyBean
    private S3Client s3Client;

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        System.setProperty("aws.accessKeyId", accessKeyId);
        System.setProperty("aws.secretAccessKey", secretAccessKey);
        doReturn(HeadObjectResponse.builder().build())
                .when(s3Client)
                .headObject(org.mockito.ArgumentMatchers.any(HeadObjectRequest.class));
    }

    @DisplayName("pre-signed url을 발급한다")
    @Test
    void start_success() throws Exception {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);
        PreSignedUrlStartRequest request = new PreSignedUrlStartRequest("image/jpeg", 1_000_000L);

        // when
        PresignedUrlStartResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when().post("/api/members/me/profile-image/pre-signed")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PresignedUrlStartResponse.class);

        // then
        URL url = new URL(response.url());
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.key()).startsWith("yagubogu/images/profiles/");
            softAssertions.assertThat(url.getHost()).contains(s3Properties.bucket());
            softAssertions.assertThat(response.url()).contains(response.key());
        });
    }

    @DisplayName("s3에 업로드된 이미지로 회원의 프로필 이미지 주소를 수정한다")
    @Test
    void completeAndUpdate_success() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);
        String key = "yagubogu/images/profiles/abc-123";

        S3Utilities utilities = s3Client.utilities();
        String expectedUrl = utilities.getUrl(b -> b.bucket(s3Properties.bucket()).key(key)).toExternalForm();

        // when
        PreSignedUrlCompleteResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new PreSignedUrlCompleteRequest(key))
                .when().post("/api/members/me/profile-image/update")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PreSignedUrlCompleteResponse.class);

        // then
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.url()).isEqualTo(expectedUrl);
            softAssertions.assertThat(updated.getImageUrl()).isEqualTo(expectedUrl);
        });
    }
}
