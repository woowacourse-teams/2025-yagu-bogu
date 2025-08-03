package com.yagubogu.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

class AuthorizationExtractorTest {

    private final AuthorizationExtractor authorizationExtractor = new AuthorizationExtractor();

    @DisplayName("토큰 헤더를 추출한다")
    @Test
    void extract() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer abc.def.ghi");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> token = authorizationExtractor.extract(webRequest);

        // then
        assertThat(token).contains("abc.def.ghi");
    }

    @DisplayName("헤더가 없는 경우 빈 객체를 반환한다")
    @Test
    void extract_empty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> token = authorizationExtractor.extract(webRequest);

        // then
        assertThat(token).isEmpty();
    }

    @DisplayName("Bearer 접두어가 아닌 경우 빈 객체를 반환한다")
    @Test
    void extract_not_bearer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token abc");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        // when
        Optional<String> token = authorizationExtractor.extract(webRequest);

        // then
        assertThat(token).isEmpty();
    }
}
