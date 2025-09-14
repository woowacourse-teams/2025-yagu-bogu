package com.yagubogu.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.config.QueryDslConfig;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberInfoResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class, QueryDslConfig.class})
@DataJpaTest
public class MemberServiceTest {

    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberFactory memberFactory;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, teamRepository);
    }

    @DisplayName("멤버가 응원하는 팀을 조회한다")
    @Test
    void findFavorite() {
        // given
        Team team = teamRepository.findByTeamCode("HT").orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(team));

        String expected = "KIA";

        // when
        MemberFavoriteResponse actual = memberService.findFavorite(member.getId());

        // then
        assertThat(actual.favorite()).isEqualTo(expected);
    }

    @DisplayName("멤버가 응원하는 팀이 없다면 null을 반환한다")
    @Test
    void findFavorite_null() {
        // given
        Team team = null;
        Member member = memberFactory.save(builder -> builder.team(team));

        // when
        MemberFavoriteResponse actual = memberService.findFavorite(member.getId());

        // then
        assertThat(actual.favorite()).isEqualTo(null);
    }

    @DisplayName("멤버의 닉네임을 조회한다")
    @Test
    void findNickname() {
        // given
        String nickname = "우가";
        Member member = memberFactory.save(builder -> builder.nickname(nickname));

        // when
        MemberNicknameResponse actual = memberService.findNickname(member.getId());

        // then
        assertThat(actual.nickname()).isEqualTo(nickname);
    }

    @DisplayName("멤버의 닉네임을 수정한다")
    @Test
    void patchNickname() {
        // given
        String oldNickname = "두리";
        Member member = memberFactory.save(builder -> builder.nickname(oldNickname));

        String newNickname = "둘이";

        // when
        MemberNicknameResponse actual = memberService.patchNickname(member.getId(),
                new MemberNicknameRequest(newNickname));

        // then
        assertThat(actual.nickname()).isEqualTo(newNickname);
    }

    @DisplayName("예외: 멤버를 찾지 못하면 예외가 발생한다")
    @Test
    void findFavorite_notFoundMember() {
        // given
        long invalidMemberId = 999L;

        // when & then
        assertThatThrownBy(() -> memberService.findFavorite(invalidMemberId))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }

    @DisplayName("회원을 탈퇴한다")
    @Test
    void removeMember() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        Long memberId = member.getId();

        // when
        memberService.removeMember(memberId);

        // then
        assertThat(memberRepository.findById(memberId)).isEmpty();
    }

    @DisplayName("회원을 탈퇴한다 - 소프트 딜리트")
    @Test
    void removeMember_softDelete() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        Long memberId = member.getId();

        // when
        memberService.removeMember(memberId);

        // then
        assertThat(memberRepository.findById(memberId)).isEmpty();
    }

    @DisplayName("팀을 등록한다")
    @Test
    void patchTeam() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);

        String teamCode = "SS";
        MemberFavoriteRequest request = new MemberFavoriteRequest(teamCode);

        // when
        memberService.updateFavorite(member.getId(), request);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(member.getTeam()).isNotNull();
            softAssertions.assertThat(member.getTeam().getTeamCode()).isEqualTo(teamCode);
        });
    }

    @DisplayName("팀을 갱신한다")
    @Test
    void modifyTeam() {
        // given
        String beforeTeamCode = "HT";
        Team team = teamRepository.findByTeamCode(beforeTeamCode).orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(team));

        String teamCode = "SS";
        MemberFavoriteRequest request = new MemberFavoriteRequest(teamCode);

        // when
        memberService.updateFavorite(member.getId(), request);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(member.getTeam().getTeamCode()).isNotEqualTo(beforeTeamCode);
            softAssertions.assertThat(member.getTeam().getTeamCode()).isEqualTo(teamCode);
        });
    }

    @DisplayName("예외: 팀 코드를 찾지 못하면 예외가 발생한다")
    @Test
    void updateTeam_notFoundTeamCode() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);

        String invalidTeamCode = "유효하지않은팀코드";
        MemberFavoriteRequest request = new MemberFavoriteRequest(invalidTeamCode);

        // when & then
        assertThatThrownBy(() -> memberService.updateFavorite(member.getId(), request))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Team is not found");
    }

    @DisplayName("회원 정보를 조회한다")
    @Test
    void findMember() {
        // given
        Team favoriteTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        Member member = memberFactory.save(builder -> builder.nickname("우가")
                .team(favoriteTeam));

        // when
        MemberInfoResponse actual = memberService.findMember(member.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.nickname()).isEqualTo(member.getNickname());
            softAssertions.assertThat(actual.favoriteTeam()).isEqualTo(member.getTeam().getShortName());
            softAssertions.assertThat(actual.createdAt()).isEqualTo(member.getCreatedAt().toLocalDate());
            softAssertions.assertThat(actual.profileImageUrl()).isEqualTo(member.getImageUrl());
        });
    }

    @DisplayName("응원하는 팀이 없는 회원의 정보를 조회한다")
    @Test
    void findMember_nullTeam() {
        // given
        Member member = memberFactory.save(builder -> builder.nickname("우가"));

        // when
        MemberInfoResponse actual = memberService.findMember(member.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.nickname()).isEqualTo(member.getNickname());
            softAssertions.assertThat(actual.favoriteTeam()).isNull();
            softAssertions.assertThat(actual.createdAt()).isEqualTo(member.getCreatedAt().toLocalDate());
            softAssertions.assertThat(actual.profileImageUrl()).isEqualTo(member.getImageUrl());
        });
    }

    @DisplayName("예외: 회원 정보를 조회하는데 해당하는 회원이 없으면 예외가 발생한다")
    @Test
    void findMember_notFoundMember() {
        // given
        long invalidMemberId = 999L;

        // when & then
        assertThatThrownBy(() -> memberService.findMember(invalidMemberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }
}
