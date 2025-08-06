package com.yagubogu.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.repository.TeamRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
public class MemberServiceTest {

    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, teamRepository);
    }

    @DisplayName("멤버가 응원하는 팀을 조회한다")
    @Test
    void findFavorite() {
        // given
        long memberId = 1L;
        String expected = "기아";

        // when
        MemberFavoriteResponse actual = memberService.findFavorite(memberId);

        // then
        assertThat(actual.favorite()).isEqualTo(expected);
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
        Long memberId = 1L;

        // when
        memberService.removeMember(memberId);

        // then
        assertThat(memberRepository.findById(memberId)).isEmpty();
    }

    @DisplayName("팀을 등록한다")
    @Test
    void patchTeam() {
        // given
        Long memberId = 11L;
        String teamCode = "SS";
        MemberFavoriteRequest request = new MemberFavoriteRequest(teamCode);

        // when
        memberService.updateFavorite(memberId, request);

        // then
        Member member = memberRepository.findById(memberId).orElseThrow();
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(member.getTeam().getTeamCode()).isEqualTo(teamCode);
            softAssertions.assertThat(member.getTeam()).isNotNull();
        });
    }

    @DisplayName("팀을 갱신한다")
    @Test
    void modifyTeam() {
        // given
        Long memberId = 1L;
        String beforeTeamCode = memberService.findFavorite(memberId).favorite();
        String newTeamCode = "SS";
        MemberFavoriteRequest request = new MemberFavoriteRequest(newTeamCode);

        // when
        memberService.updateFavorite(memberId, request);

        // then
        Member member = memberRepository.findById(memberId).orElseThrow();
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(member.getTeam().getTeamCode()).isEqualTo(newTeamCode);
            softAssertions.assertThat(member.getTeam().getShortName()).isNotEqualTo(beforeTeamCode);
        });
    }

    @DisplayName("예외: 팀 코드를 찾지 못하면 예외가 발생한다")
    @Test
    void updateTeam_notFoundTeamCode() {
        // given
        Long memberId = 1L;
        String invalidTeamCode = "유효하지않은팀코드";
        MemberFavoriteRequest request = new MemberFavoriteRequest(invalidTeamCode);

        // when & then
        assertThatThrownBy(() -> memberService.updateFavorite(memberId, request))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Team is not found");
    }
}
