package com.yagubogu.auth.support;

import com.yagubogu.auth.config.AppleAuthProperties;
import com.yagubogu.auth.dto.AppleAuthParam;
import com.yagubogu.auth.exception.InvalidTokenException;
import com.yagubogu.member.domain.OAuthProvider;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppleAuthValidator implements AuthValidator<AppleAuthParam> {

    private final AppleAuthProperties appleAuthProperties;

    public AppleAuthValidator(final AppleAuthProperties appleAuthProperties) {
        this.appleAuthProperties = appleAuthProperties;
    }

    @Override
    public boolean supports(final OAuthProvider provider) {
        return provider.isApple();
    }

    @Override
    public void validate(final AppleAuthParam response) {
        if (response.iss() == null || !appleAuthProperties.issuer().equals(response.iss())) {
            throw new InvalidTokenException("Invalid issuer");
        }

        List<String> expectedAudiences = Arrays.stream(appleAuthProperties.clientId().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();

        if (response.aud() == null || response.aud().isEmpty() ||
                expectedAudiences.stream().noneMatch(expected -> response.aud().contains(expected))) {
            log.warn("Validating Apple token audience. expectedClientIds={}, actualAud={}",
                    expectedAudiences, response.aud());
            throw new InvalidTokenException("Invalid audience");
        }

        Long exp = response.exp();
        if (exp == null || Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
            throw new InvalidTokenException("Token expired");
        }
    }
}
