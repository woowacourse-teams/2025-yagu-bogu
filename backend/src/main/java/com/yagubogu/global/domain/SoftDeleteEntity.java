package com.yagubogu.global.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@MappedSuperclass
@SQLDelete(sql = "UPDATE members SET deleted_at = now() WHERE member_id = ?")
@SQLRestriction("deleted_at IS NULL")
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;
}
