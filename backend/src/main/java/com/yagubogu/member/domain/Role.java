package com.yagubogu.member.domain;

public enum Role {

    USER(1),
    ADMIN(2)
    ;

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public boolean hasPermission(Role required) {
        return this.level >= required.level;
    }
}
