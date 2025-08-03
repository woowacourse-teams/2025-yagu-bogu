package com.yagubogu.member.repository;

import com.yagubogu.member.domain.Member;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
public class CustomMemberRepositoryImpl implements CustomMemberRepository {

    private final EntityManager em;

    @Transactional
    @Override
    public void softDeleteById(Long id) {
        Member member = em.find(Member.class, id);
        if (member != null) {
            member.markDeleted();
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Member> findActiveById(Long id) {
        String jpql = "SELECT m FROM Member m WHERE m.id = :id AND m.deleted = false";
        return em.createQuery(jpql, Member.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }
}
