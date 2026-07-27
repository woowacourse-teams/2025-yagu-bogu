package com.yagubogu.reward.dto.v1;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GifticonRecipientRequestTest {

    @DisplayName("일반적인 휴대폰 번호 형식을 허용한다")
    @Test
    void acceptValidPhoneNumberFormats() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertThat(factory.getValidator().validate(new GifticonRecipientRequest("01012345678")))
                    .isEmpty();
            assertThat(factory.getValidator().validate(new GifticonRecipientRequest("010-1234-5678")))
                    .isEmpty();
            assertThat(factory.getValidator().validate(new GifticonRecipientRequest("010 1234 5678")))
                    .isEmpty();
        }
    }

    @DisplayName("휴대폰 번호 형식이 아니면 요청 검증에 실패한다")
    @Test
    void rejectInvalidPhoneNumberFormat() {
        GifticonRecipientRequest request = new GifticonRecipientRequest("abc");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<GifticonRecipientRequest>> violations =
                    factory.getValidator().validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Invalid recipient phone number");
        }
    }
}
