package com.yagubogu.auth.gateway;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yagubogu.auth.config.AppleAuthProperties;
import com.yagubogu.auth.dto.AppleAuthParam;
import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.exception.InvalidTokenException;
import com.yagubogu.member.domain.OAuthProvider;
import java.security.interfaces.RSAPublicKey;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!local")
@Component
public class AppleAuthGateway implements ProviderAuthGateway {

    private final JwkProvider appleJwkProvider;
    private final AppleAuthProperties appleAuthProperties;

    public AppleAuthGateway(
            final JwkProvider appleJwkProvider,
            final AppleAuthProperties appleAuthProperties
    ) {
        this.appleJwkProvider = appleJwkProvider;
        this.appleAuthProperties = appleAuthProperties;
    }

    @Override
    public boolean supports(final OAuthProvider provider) {
        return provider.isApple();
    }

    @Override
    public AuthParam validateToken(final LoginParam loginParam) {
        String idToken = loginParam.idToken();
        try {
            DecodedJWT decoded = JWT.decode(idToken);
            String keyId = decoded.getKeyId();
            if (keyId == null || keyId.isBlank()) {
                throw new InvalidTokenException("Apple token kid is missing");
            }

            Jwk jwk = appleJwkProvider.get(keyId);
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(appleAuthProperties.issuer())
                    .build();

            DecodedJWT verified = verifier.verify(idToken);
            return AppleAuthParam.from(verified);
        } catch (JwkException | JWTVerificationException ex) {
            throw new InvalidTokenException("Invalid Apple token");
        }
    }
}
