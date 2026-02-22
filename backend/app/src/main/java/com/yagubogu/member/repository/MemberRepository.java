package com.yagubogu.member.repository;

import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Nickname;
import com.yagubogu.member.domain.OAuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByOauthIdAndProviderAndDeletedAtIsNull(String oauthId, OAuthProvider provider);

    long countByDeletedAtIsNull();

    boolean existsByNickname(Nickname nickname);
}
