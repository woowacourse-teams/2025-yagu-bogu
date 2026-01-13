package com.yagubogu.auth.domain;

import com.yagubogu.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "refresh_tokens")
@Entity
public class RefreshToken {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked", nullable = false, columnDefinition = "boolean default false")
    private boolean isRevoked = false;

    public RefreshToken(final String id, final Member member, final Instant expiresAt) {
        this.id = id;
        this.member = member;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken generate(Member member, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID().toString(), member, expiresAt);
    }

    public boolean isInValid() {
        return isRevoked || Instant.now().isAfter(expiresAt);
    }

    public void revoke() {
        this.isRevoked = true;
    }
}
