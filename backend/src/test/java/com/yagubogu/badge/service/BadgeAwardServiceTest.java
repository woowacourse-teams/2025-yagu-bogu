package com.yagubogu.badge.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.support.badge.MemberBadgeFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
public class BadgeAwardServiceTest {

    private BadgeAwardService badgeAwardService;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private MemberBadgeFactory memberBadgeFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private MemberBadgeRepository memberBadgeRepository;

    @BeforeEach
    public void setUp() {
        this.badgeAwardService = new BadgeAwardService(memberBadgeRepository);
    }

    @DisplayName("새롭게 획득하는 뱃지일 경우, MemberBadge를 생성하고 저장한다")
    @Test
    void award() {
        // given
        Team team = teamRepository.findByTeamCode("HT").orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(team)
                .nickname("우가")
        );
        Badge leadOff = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        BadgeAwardCandidate candidate = new BadgeAwardCandidate(member, List.of(leadOff));

        // when
        badgeAwardService.award(candidate);

        // then
        assertThat(memberBadgeRepository.findByMemberAndBadge(member, leadOff))
                .isPresent()
                .hasValueSatisfying(memberBadge -> {
                    assertThat(memberBadge.getBadge()).isEqualTo(leadOff);
                    assertThat(memberBadge.getMember()).isEqualTo(member);
                });
    }

    @DisplayName("이미 획득 과정에 있는 뱃지일 경우, 기존 MemberBadge의 progress를 증가시킨다")
    @Test
    void award_increasesProgress_whenMemberBadgeAlreadyExists() {
        // given
        Team team = teamRepository.findByTeamCode("HT").orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(team).nickname("우가"));
        Badge signUpBadge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        MemberBadge memberBadge = memberBadgeFactory.save(builder -> builder.member(member)
                .badge(signUpBadge)
                .isAchieved(true)
                .build()
        );
        BadgeAwardCandidate candidateForSecondAward = new BadgeAwardCandidate(member, List.of(signUpBadge));

        // when
        badgeAwardService.award(candidateForSecondAward);

        // then
        assertThat(memberBadgeRepository.findByMemberAndBadge(member, signUpBadge))
                .isPresent()
                .hasValueSatisfying(updatedMemberBadge -> {
                    assertThat(updatedMemberBadge.getProgress()).isEqualTo(1);
                    assertThat(updatedMemberBadge.getId()).isEqualTo(memberBadge.getId());
                });
    }
}
