package com.yagubogu.support.member;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;
import com.yagubogu.team.domain.Team;
import java.util.UUID;

public class MemberBuilder {

    private Team team;
    private String nickname = "user-" + UUID.randomUUID();
    private String email = UUID.randomUUID() + "email@gmail.com";
    private OAuthProvider provider = OAuthProvider.GOOGLE;
    private String oauthId = UUID.randomUUID().toString();
    private Role role = Role.USER;
    private String imageUrl = "image.png";
    private Badge representativeBadge = null;

    public MemberBuilder team(final Team team) {
        this.team = team;

        return this;
    }

    public MemberBuilder nickname(final String nickname) {
        this.nickname = nickname;

        return this;
    }

    public MemberBuilder role(final Role role) {
        this.role = role;

        return this;
    }

    public MemberBuilder representativeBadge(final Badge badge) {
        this.representativeBadge = badge;

        return this;
    }

    public Member build() {
        return new Member(team, nickname, email, provider, oauthId, role, imageUrl, representativeBadge);
    }
}
