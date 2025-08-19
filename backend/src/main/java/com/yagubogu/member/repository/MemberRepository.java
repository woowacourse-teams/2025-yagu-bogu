package com.yagubogu.member.repository;

import com.yagubogu.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByOauthId(String oauthId);

    @Query("select m.team.id from Member m where m.id = :memberId")
    Optional<Long> findTeamIdById(Long memberId);
}
