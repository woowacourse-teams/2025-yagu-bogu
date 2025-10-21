package com.yagubogu.member.repository;

import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Nickname;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByOauthIdAndDeletedAtIsNull(String oauthId);

    long countByDeletedAtIsNull();

    boolean existsByNickname(Nickname nickname);
}
