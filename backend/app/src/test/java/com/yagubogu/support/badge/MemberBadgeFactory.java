package com.yagubogu.support.badge;

import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import java.util.function.Consumer;

public class MemberBadgeFactory {

    private final MemberBadgeRepository memberBadgeRepository;

    public MemberBadgeFactory(final MemberBadgeRepository memberBadgeRepository) {
        this.memberBadgeRepository = memberBadgeRepository;
    }

    public MemberBadge save(final Consumer<MemberBadgeBuilder> customizer) {
        MemberBadgeBuilder builder = new MemberBadgeBuilder();
        customizer.accept(builder);
        MemberBadge memberBadge = builder.build();

        return memberBadgeRepository.save(memberBadge);
    }
}
