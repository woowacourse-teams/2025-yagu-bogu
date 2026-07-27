package com.yagubogu.reward.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KakaoGiftPropertiesTest {

    @DisplayName("카카오 인증 설정이 누락되면 설정 검증에 실패한다")
    @Test
    void rejectMissingCredentials() {
        KakaoGiftProperties properties = new KakaoGiftProperties(
                "https://gift.example.com",
                null,
                " ",
                Duration.ofSeconds(1),
                Duration.ofSeconds(1)
        );

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<KakaoGiftProperties>> violations = factory.getValidator().validate(properties);

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("apiKey", "templateToken");
        }
    }
}
