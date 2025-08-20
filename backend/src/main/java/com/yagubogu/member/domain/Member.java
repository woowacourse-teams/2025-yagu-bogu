package com.yagubogu.member.domain;

import com.yagubogu.team.domain.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@SQLDelete(sql = "UPDATE members SET is_deleted = true WHERE member_id = ?")
@SQLRestriction("is_deleted = false")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
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

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public boolean isSameId(long memberId) {
        return this.id.equals(memberId);
    }

    public void updateFavorite(final Team team) {
        this.team = team;
    }

    public void updateNickname(final String nickname) {
        this.nickname = nickname;
    }
}
