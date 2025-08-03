package com.yagubogu.member.repository;

import com.yagubogu.member.domain.Member;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CustomMemberRepositoryImpl implements CustomMemberRepository {

    private final EntityManager em;

    @Override
    public void softDeleteById(Long id) {
        Member member = em.find(Member.class, id);
        if (member != null) {
            member.markDeleted();
        }
    }

    @Override
    public Optional<Member> findActiveById(Long id) {
        String jpql = "SELECT m FROM Member m WHERE m.id = :id AND m.deleted = false";
        return em.createQuery(jpql, Member.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }
}
