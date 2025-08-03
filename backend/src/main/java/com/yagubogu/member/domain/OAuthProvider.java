package com.yagubogu.member.domain;

public enum OAuthProvider {

    GOOGLE;

    public boolean isGoogle() {
        return this == GOOGLE;
    }
}
