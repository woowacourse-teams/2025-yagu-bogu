package com.yagubogu.member.domain;

public enum OAuthProvider {

    GOOGLE,
    APPLE,
    ;

    public boolean isGoogle() {
        return this == GOOGLE;
    }

    public boolean isApple() {
        return this == APPLE;
    }
}
