package com.yagubogu.reward.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GifticonReconciliationPropertiesTest {

    @DisplayName("최초 대사 대기 시간이 누락되면 설정 검증에 실패한다")
    @Test
    void rejectMissingInitialDelay() {
        GifticonReconciliationProperties properties =
                new GifticonReconciliationProperties(null);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<GifticonReconciliationProperties>> violations =
                    factory.getValidator().validate(properties);

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("initialDelay");
        }
    }

    @DisplayName("최초 대사 대기 시간은 양수여야 한다")
    @Test
    void rejectNonPositiveInitialDelay() {
        GifticonReconciliationProperties properties =
                new GifticonReconciliationProperties(Duration.ZERO);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<GifticonReconciliationProperties>> violations =
                    factory.getValidator().validate(properties);

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("initialDelayPositive");
        }
    }
}
