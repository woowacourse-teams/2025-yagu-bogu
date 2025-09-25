package com.yagubogu.auth.support;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yagubogu.auth.config.AuthTokenProperties;
import com.yagubogu.auth.config.AuthTokenProperties.TokenProperties;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Role;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@EnableConfigurationProperties(AuthTokenProperties.class)
@Component
public class AuthTokenProvider {

    private static final String ROLE = "role";

    private final AuthTokenProperties authTokenProperties;

    public AuthTokenProvider(final AuthTokenProperties authTokenProperties) {
        this.authTokenProperties = authTokenProperties;
    }

    public String issueAccessToken(final MemberClaims memberClaims) {
        TokenProperties accessTokenProperties = authTokenProperties.getAccessToken();

        return createToken(memberClaims, accessTokenProperties);
    }

    public String createRefreshToken(final MemberClaims memberClaims) {
        TokenProperties refreshTokenProperties = authTokenProperties.getRefreshToken();

        return createToken(memberClaims, refreshTokenProperties);
    }

    public void validateAccessToken(final String token) {
        try {
            verifyAccessToken(token);
        } catch (TokenExpiredException e) {
            throw new UnAuthorizedException("Expired token");
        } catch (JWTVerificationException e) {
            throw new UnAuthorizedException("Invalid token");
        }
    }

    public Long getMemberIdByAccessToken(final String token) {
        try {
            return Long.parseLong(verifyAccessToken(token).getSubject());
        } catch (JWTVerificationException e) {
            throw new UnAuthorizedException("Invalid token");
        }
    }

    public Role getRoleByAccessToken(final String token) {
        try {
            return Role.valueOf(verifyAccessToken(token).getClaim(ROLE).asString());
        } catch (JWTVerificationException e) {
            throw new UnAuthorizedException("Invalid token");
        }
    }

    private String createToken(
            final MemberClaims memberClaims,
            final TokenProperties tokenProperties
    ) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validity = now.plusSeconds(tokenProperties.getExpiresIn());
        Algorithm algorithm = Algorithm.HMAC256(tokenProperties.getSecretKey());

        return JWT.create()
                .withSubject(memberClaims.id().toString())
                .withClaim(ROLE, memberClaims.role().name())
                .withIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .withExpiresAt(Date.from(validity.atZone(ZoneId.systemDefault()).toInstant()))
                .sign(algorithm);
    }

    private DecodedJWT verifyAccessToken(final String token) {
        Algorithm algorithm = Algorithm.HMAC256(authTokenProperties.getAccessToken().getSecretKey());
        JWTVerifier verifier = JWT.require(algorithm).build();

        return verifier.verify(token);
    }
}
