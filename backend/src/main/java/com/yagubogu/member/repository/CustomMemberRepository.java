package com.yagubogu.member.repository;

import com.yagubogu.member.domain.Member;
import java.util.Optional;

public interface CustomMemberRepository {

    void softDeleteById(Long id);

    Optional<Member> findActiveById(Long id);
}
