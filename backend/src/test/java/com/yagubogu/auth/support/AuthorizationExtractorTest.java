package com.yagubogu.auth.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class AuthorizationExtractorTest {

    private final AuthorizationExtractor authorizationExtractor = new AuthorizationExtractor();

    @DisplayName("NativeWebRequest로부터 토큰 헤더를 추출한다")
    @Test
    void extract_NativeWebRequest() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc.def.ghi");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> token = authorizationExtractor.extract(webRequest);

        // then
        assertThat(token).contains("abc.def.ghi");
    }

    @DisplayName("NativeWebRequest의 헤더가 없는 경우 빈 객체를 반환한다")
    @Test
    void extract_NativeWebRequest_empty() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> token = authorizationExtractor.extract(webRequest);

        // then
        assertThat(token).isEmpty();
    }

    @DisplayName("NativeWebRequest가 Bearer 접두어가 아닌 경우 빈 객체를 반환한다")
    @Test
    void extract_NativeWebRequest_not_bearer() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token abc");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> token = authorizationExtractor.extract(webRequest);

        // then
        assertThat(token).isEmpty();
    }

    @DisplayName("HttpServletRequest의 토큰 헤더를 추출한다")
    @Test
    void extract_HttpServletRequest() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc.def.ghi");

        // when
        Optional<String> token = authorizationExtractor.extract(request);

        // then
        assertThat(token).contains("abc.def.ghi");
    }

    @DisplayName("HttpServletRequest의 헤더가 없는 경우 빈 객체를 반환한다")
    @Test
    void extract_HttpServletRequest_empty() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        Optional<String> token = authorizationExtractor.extract(request);

        // then
        assertThat(token).isEmpty();
    }

    @DisplayName("HttpServletRequest가 Bearer 접두어가 아닌 경우 빈 객체를 반환한다")
    @Test
    void extract_HttpServletRequest_not_bearer() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token abc");

        // when
        Optional<String> token = authorizationExtractor.extract(request);

        // then
        assertThat(token).isEmpty();
    }

    @DisplayName("HttpServletRequest에 여러 Authorization 헤더가 있을 때 Bearer 토큰을 추출한다")
    @Test
    void extract_HttpServletRequest_multiple_headers() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token xyz");
        request.addHeader("Authorization", "Bearer abc.def.ghi");

        // when
        Optional<String> token = authorizationExtractor.extract(request);

        // then
        assertThat(token).contains("abc.def.ghi");
    }
}
