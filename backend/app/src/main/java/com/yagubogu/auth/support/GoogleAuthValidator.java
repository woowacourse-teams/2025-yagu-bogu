package com.yagubogu.auth.support;

import com.yagubogu.auth.config.GoogleAuthProperties;
import com.yagubogu.auth.dto.GoogleAuthParam;
import com.yagubogu.auth.exception.InvalidTokenException;
import com.yagubogu.member.domain.OAuthProvider;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleAuthValidator implements AuthValidator<GoogleAuthParam> {

    private static final String ISSUER_GOOGLE = "https://accounts.google.com";
    private static final String ISSUER_GOOGLE_NO_SCHEME = "accounts.google.com";

    private final GoogleAuthProperties googleAuthProperties;

    public GoogleAuthValidator(final GoogleAuthProperties googleAuthProperties) {
        this.googleAuthProperties = googleAuthProperties;
    }

    @Override
    public boolean supports(final OAuthProvider provider) {
        return provider.isGoogle();
    }

    @Override
    public void validate(final GoogleAuthParam response) {
        String iss = response.iss();
        if (!(ISSUER_GOOGLE.equals(iss) || ISSUER_GOOGLE_NO_SCHEME.equals(iss))) {
            throw new InvalidTokenException("Invalid issuer");
        }

        List<String> expectedAudiences = Arrays.stream(googleAuthProperties.clientId().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();

        if (response.aud() == null || expectedAudiences.stream().noneMatch(expected -> expected.equals(response.aud()))) {
            log.warn("Validating Google token audience. expectedClientIds={}, actualAud={}",
                    expectedAudiences, response.aud());

            throw new InvalidTokenException("Invalid audience");
        }

        long expEpoch = response.exp();
        if (Instant.ofEpochSecond(expEpoch).isBefore(Instant.now())) {
            throw new InvalidTokenException("Token expired");
        }
    }
}
