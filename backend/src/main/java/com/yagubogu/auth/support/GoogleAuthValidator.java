package com.yagubogu.auth.support;

import com.yagubogu.auth.config.GoogleAuthProperties;
import com.yagubogu.auth.dto.GoogleAuthResponse;
import com.yagubogu.auth.exception.InvalidTokenException;
import com.yagubogu.member.domain.OAuthProvider;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleAuthValidator implements AuthValidator<GoogleAuthResponse> {

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
    public void validate(final GoogleAuthResponse response) {
        String iss = response.iss();
        if (!(ISSUER_GOOGLE.equals(iss) || ISSUER_GOOGLE_NO_SCHEME.equals(iss))) {
            throw new InvalidTokenException("Invalid issuer");
        }

        if (!googleAuthProperties.clientId().equals(response.aud())) {
            String expectedClientId = googleAuthProperties.clientId();
            String actualAud = response.aud();

            log.info("Validating Google token audience. expectedClientId={}, actualAud={}",
                    expectedClientId, actualAud);

            throw new InvalidTokenException("Invalid audience");
        }

        long expEpoch = response.exp();
        if (Instant.ofEpochSecond(expEpoch).isBefore(Instant.now())) {
            throw new InvalidTokenException("Token expired");
        }
    }
}
