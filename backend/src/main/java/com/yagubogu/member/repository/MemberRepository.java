package com.yagubogu.member.repository;

import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Nickname;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByOauthIdAndDeletedAtIsNull(String oauthId);

    @Query("select m.team.id from Member m where m.id = :memberId and m.deletedAt is null")
    Optional<Long> findTeamIdById(Long memberId);

    boolean existsByNickname(Nickname nickname);
}
