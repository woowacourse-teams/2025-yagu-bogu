package com.yagubogu.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Nickname;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;

public record GoogleAuthParam(
        String iss,
        String oauthId,
        String azp,
        String aud,
        Long iat,
        Long exp,
        String email,
        boolean emailVerified,
        String name,
        String picture,
        String givenName,
        String familyName,
        String locale
) implements AuthParam {

    @JsonCreator
    public static GoogleAuthParam create(
            @JsonProperty("iss") String iss,
            @JsonProperty("sub") String sub,
            @JsonProperty("azp") String azp,
            @JsonProperty("aud") String aud,
            @JsonProperty("iat") String iat,
            @JsonProperty("exp") String exp,
            @JsonProperty("email") String email,
            @JsonProperty("email_verified") String emailVerified,
            @JsonProperty("name") String name,
            @JsonProperty("picture") String picture,
            @JsonProperty("given_name") String givenName,
            @JsonProperty("family_name") String familyName,
            @JsonProperty("locale") String locale
    ) {
        return new GoogleAuthParam(
                iss,
                sub,
                azp,
                aud,
                Long.parseLong(iat),
                Long.parseLong(exp),
                email,
                Boolean.parseBoolean(emailVerified),
                name,
                picture,
                givenName,
                familyName,
                locale
        );
    }

    @Override
    public Member toMember() {
        return new Member(null, new Nickname(name), email, OAuthProvider.GOOGLE, oauthId, Role.USER, picture, null);
    }
}
