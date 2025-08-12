package com.yagubogu.support.member;

import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import java.util.function.Consumer;

public class MemberFactory {

    private final MemberRepository memberRepository;

    public MemberFactory(
            final MemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    public Member save(final Consumer<MemberBuilder> customizer) {
        MemberBuilder builder = new MemberBuilder();
        customizer.accept(builder);
        Member member = builder.build();

        return memberRepository.save(member);
    }
}
