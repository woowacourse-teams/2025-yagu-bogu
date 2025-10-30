package com.yagubogu.talk.domain;

import com.yagubogu.game.domain.Game;
import com.yagubogu.global.domain.BaseEntity;
import com.yagubogu.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE talks SET deleted_at = now() WHERE talk_id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "talks",
        indexes = {
                @Index(name = "idx_game_member_content_created",
                        columnList = "game_id, member_id, content, created_at"),
                @Index(name = "idx_client_message_id",
                        columnList = "client_message_id")
        }
)
@Entity
public class Talk extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "talk_id")
    private Long id;

    @Column(name = "client_message_id", unique = true, nullable = false)
    private String clientMessageId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Talk(
            final String clientMessageId,
            final Game game,
            final Member member,
            final String content,
            final LocalDateTime createdAt
    ) {
        this.clientMessageId = clientMessageId;
        this.game = game;
        this.member = member;
        this.content = content;
        this.createdAt = createdAt;
    }
}
