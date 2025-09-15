package com.yagubogu.like.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LikeWindowRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public boolean tryInsertWindow(long gameId, String clientId, long windowStart) {
        int updated = em.createNativeQuery("""
                        insert ignore into like_windows (game_id, client_instance_id, window_start_epoch_sec)
                        values (:gameId, :clientId, :windowStart)
                        """)
                .setParameter("gameId", gameId)
                .setParameter("clientId", clientId)
                .setParameter("windowStart", windowStart)
                .executeUpdate();
        return updated > 0;
    }
}
