package com.yagubogu.member.domain;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@SQLDelete(sql = "UPDATE members SET is_deleted = true WHERE member_id = ?")
@Where(clause = "is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"oauth_id", "provider"})
        }
)
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;

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

    public void updateNickname(final String nickname) {
        this.nickname = nickname;
    }
}
