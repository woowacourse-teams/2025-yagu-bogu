package com.yagubogu.auth.dto;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Nickname;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

public record AppleAuthParam(
        String iss,
        String oauthId,
        List<String> aud,
        Long iat,
        Long exp,
        String email,
        boolean emailVerified,
        boolean isPrivateEmail) implements AuthParam {

    private static final int NICKNAME_MAX_LENGTH = 25;
    private static final int OAUTH_SUFFIX_LENGTH = 8;

    public static AppleAuthParam from(final DecodedJWT jwt) {
        String emailVerified = jwt.getClaim("email_verified").asString();
        String isPrivateEmail = jwt.getClaim("is_private_email").asString();

        Instant issuedAt = jwt.getIssuedAt() == null ? null : jwt.getIssuedAt().toInstant();
        Instant expiresAt = jwt.getExpiresAt() == null ? null : jwt.getExpiresAt().toInstant();

        return new AppleAuthParam(
                jwt.getIssuer(),
                jwt.getSubject(),
                jwt.getAudience(),
                issuedAt == null ? null : issuedAt.getEpochSecond(),
                expiresAt == null ? null : expiresAt.getEpochSecond(),
                jwt.getClaim("email").asString(),
                "true".equalsIgnoreCase(emailVerified),
                "true".equalsIgnoreCase(isPrivateEmail));
    }

    @Override
    public String picture() {
        return "";
    }

    @Override
    public Member toMember(String defaultImageUrl) {
        String safeEmail = email == null || email.isBlank()
                ? oauthId + "@appleid.apple"
                : email;
        String nickname = generateNickname(oauthId);

        return new Member(null, new Nickname(nickname), safeEmail, OAuthProvider.APPLE, oauthId, Role.USER, defaultImageUrl, null);
    }

    private static String generateNickname(final String oauthId) {
        String suffix = oauthId == null ? "user"
                : oauthId.substring(0, Math.min(OAUTH_SUFFIX_LENGTH, oauthId.length()));
        String nickname = "apple_" + suffix.toLowerCase(Locale.ROOT);
        if (nickname.length() > NICKNAME_MAX_LENGTH) {
            return nickname.substring(0, NICKNAME_MAX_LENGTH);
        }
        return nickname;
    }
}
