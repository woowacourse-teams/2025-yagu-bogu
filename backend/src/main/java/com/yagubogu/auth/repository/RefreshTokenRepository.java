package com.yagubogu.auth.repository;

import com.yagubogu.auth.domain.RefreshToken;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    List<RefreshToken> findAllByMemberId(Long memberId);
}
