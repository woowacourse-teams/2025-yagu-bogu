package com.yagubogu.auth.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class AuthorizationExtractorTest {

    private static final String BEARER = "Bearer ";

    private final AuthorizationExtractor authorizationExtractor = new AuthorizationExtractor();

    @DisplayName("NativeWebRequest로부터 토큰 헤더를 추출한다")
    @Test
    void extract_NativeWebRequest() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "abc.def.ghi";
        request.addHeader(HttpHeaders.AUTHORIZATION, BEARER + token);
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> extractedToken = authorizationExtractor.extract(webRequest);

        // then
        assertThat(extractedToken).contains(token);
    }

    @DisplayName("NativeWebRequest의 헤더가 없는 경우 빈 객체를 반환한다")
    @Test
    void extract_NativeWebRequest_empty() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> extractedToken = authorizationExtractor.extract(webRequest);

        // then
        assertThat(extractedToken).isEmpty();
    }

    @DisplayName("NativeWebRequest가 Bearer 접두어가 아닌 경우 빈 객체를 반환한다")
    @Test
    void extract_NativeWebRequest_not_bearer() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String invalidToken = "Token abc";
        request.addHeader(HttpHeaders.AUTHORIZATION, invalidToken);
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> extractedToken = authorizationExtractor.extract(webRequest);

        // then
        assertThat(extractedToken).isEmpty();
    }

    @DisplayName("HttpServletRequest의 토큰 헤더를 추출한다")
    @Test
    void extract_HttpServletRequest() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "abc.def.ghi";
        request.addHeader(HttpHeaders.AUTHORIZATION, BEARER + token);

        // when
        Optional<String> extractedToken = authorizationExtractor.extract(request);

        // then
        assertThat(extractedToken).contains(token);
    }

    @DisplayName("HttpServletRequest의 헤더가 없는 경우 빈 객체를 반환한다")
    @Test
    void extract_HttpServletRequest_empty() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        Optional<String> extractedToken = authorizationExtractor.extract(request);

        // then
        assertThat(extractedToken).isEmpty();
    }

    @DisplayName("HttpServletRequest가 Bearer 접두어가 아닌 경우 빈 객체를 반환한다")
    @Test
    void extract_HttpServletRequest_not_bearer() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String invalidToken = "Token abc";
        request.addHeader(HttpHeaders.AUTHORIZATION, invalidToken);

        // when
        Optional<String> extractedToken = authorizationExtractor.extract(request);

        // then
        assertThat(extractedToken).isEmpty();
    }

    @DisplayName("HttpServletRequest에 여러 Authorization 헤더가 있을 때 Bearer 토큰을 추출한다")
    @Test
    void extract_HttpServletRequest_multiple_headers() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Token xyz");
        String token = "abc.def.ghi";
        request.addHeader(HttpHeaders.AUTHORIZATION, BEARER + token);

        // when
        Optional<String> extractedToken = authorizationExtractor.extract(request);

        // then
        assertThat(extractedToken).contains(token);
    }
}
