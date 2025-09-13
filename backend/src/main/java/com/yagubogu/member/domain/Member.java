package com.yagubogu.member.domain;

import com.yagubogu.global.domain.BaseEntity;
import com.yagubogu.team.domain.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE members SET deleted_at = now() WHERE member_id = ?")
@Table(name = "members")
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @Column(name = "nickname", unique = true, nullable = false)
    private String nickname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Column(name = "image_url")
    private String imageUrl;

    public Member(final Team team, final String nickname, final String email, final OAuthProvider provider,
                  final String oauthId, final Role role, final String imageUrl) {
        this.team = team;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.oauthId = oauthId;
        this.role = role;
        this.imageUrl = imageUrl;
    }

    public boolean isAdmin() {
        return role.equals(Role.ADMIN);
    }

    public boolean isSameId(final long memberId) {
        return this.id.equals(memberId);
    }

    public void updateFavorite(final Team team) {
        this.team = team;
    }

    public void updateNickname(final String nickname) {
        this.nickname = nickname;
    }
}
