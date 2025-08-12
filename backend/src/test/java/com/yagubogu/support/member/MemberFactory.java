package com.yagubogu.support.member;

import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.util.function.Consumer;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class MemberFactory {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    public MemberFactory(
            final MemberRepository memberRepository,
            final TeamRepository teamRepository
    ) {
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
    }

    public Member save(final Consumer<MemberBuilder> customizer) {
        MemberBuilder builder = new MemberBuilder();
        customizer.accept(builder);
        Member member = builder.build();

        Team team = member.getTeam();
        if (team != null) {
            teamRepository.save(team);
        }

        return memberRepository.save(member);
    }
}
