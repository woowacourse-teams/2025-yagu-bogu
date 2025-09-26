package com.yagubogu.member.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeListResponse;
import com.yagubogu.badge.dto.BadgeResponseWithRates;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberInfoResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.support.badge.MemberBadgeFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
public class MemberServiceTest {

    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private MemberBadgeRepository memberBadgeRepository;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private MemberBadgeFactory memberBadgeFactory;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, teamRepository, badgeRepository, memberBadgeRepository);
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

    @DisplayName("예외: 닉네임 수정시 존재하는 닉네임이면 예외가 발생한다")
    @Test
    void patchNickname_duplicateNickname() {
        // given
        String existNickname = "존재하는닉네임";
        memberFactory.save(builder -> builder.nickname(existNickname));
        Member member = memberFactory.save(builder -> builder.nickname("우가"));
        MemberNicknameRequest request = new MemberNicknameRequest(existNickname);

        // when & then
        assertThatThrownBy(() -> memberService.patchNickname(member.getId(), request))
                .isExactlyInstanceOf(ConflictException.class)
                .hasMessage("Nickname already exists: " + existNickname);
    }

    @DisplayName("예외: 닉네임 수정 시 최대 길이를 초과하면 예외가 발생한다")
    @Test
    void patchNickname_nickNameTooLong() {
        // given
        Member member = memberFactory.save(builder -> builder.nickname("우가"));

        String longNickName = "12345678901234567890123456";
        MemberNicknameRequest request = new MemberNicknameRequest(longNickName);

        // when & then
        assertThatThrownBy(() -> memberService.patchNickname(member.getId(), request))
                .isExactlyInstanceOf(UnprocessableEntityException.class)
                .hasMessage("Nickname must be " + 25 + " characters or fewer.");
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
            softAssertions.assertThat(actual.nickname()).isEqualTo(member.getNickname().getValue());
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
            softAssertions.assertThat(actual.nickname()).isEqualTo(member.getNickname().getValue());
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

    @DisplayName("뱃지를 조회한다")
    @Test
    void findBadges() {
        // given
        Badge badge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        Member member = memberFactory.save(builder -> builder.nickname("우가"));
        long memberId = member.getId();
        memberBadgeFactory.save(builder ->
                builder.badge(badge)
                        .member(member)
                        .isAchieved(true)
        );

        List<BadgeResponseWithRates> badgeResponses = List.of(
                new BadgeResponseWithRates(
                        1L, "리드오프", "회원가입한 회원",
                        Policy.SIGN_UP, true, LocalDateTime.now(),
                        100.0, 100.0, "https://github.com/user-attachments/assets/68f40c11-e0ac-4917-9cab-d482bd44bdea"
                ),
                new BadgeResponseWithRates(
                        2L, "말문이 트이다", "첫 현장톡 작성",
                        Policy.CHAT, false, null,
                        0.0, 0.0, "https://github.com/user-attachments/assets/7f6cc5ae-e4af-41c7-96f1-e531c661f771"
                ),
                new BadgeResponseWithRates(
                        3L, "공포의 주둥아리", "현장톡 누적 100회",
                        Policy.CHAT, false, null,
                        0.0, 0.0, "https://github.com/user-attachments/assets/b393d494-7168-4c4a-821d-113db6f6d7f0"
                ),
                new BadgeResponseWithRates(
                        4L, "플레이볼", "첫 직관 인증",
                        Policy.CHECK_IN, false, null,
                        0.0, 0.0, "https://github.com/user-attachments/assets/36a27348-0870-4910-b106-c35319eb4ac6"
                ),
                new BadgeResponseWithRates(
                        5L, "그랜드슬램", "9개 전구장 방문",
                        Policy.GRAND_SLAM, false, null,
                        0.0, 0.0, "https://github.com/user-attachments/assets/7ef1ead4-78cf-472e-a610-48f9d0439ded"
                )
        );

        BadgeListResponse expected = BadgeListResponse.from(member.getRepresentativeBadge(), badgeResponses);

        // when
        BadgeListResponse actual = memberService.findBadges(memberId);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.representativeBadge())
                    .isEqualTo(expected.representativeBadge());
            softAssertions.assertThat(actual.badges())
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("achievedAt")
                    .containsExactlyInAnyOrderElementsOf(expected.badges());
        });
    }

    @DisplayName("대표 뱃지 수정한다")
    @Test
    void patchRepresentativeBadge() {
        // given
        Badge badge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        Member member = memberFactory.save(builder -> builder.nickname("우가"));
        memberBadgeFactory.save(builder ->
                builder.member(member)
                        .badge(badge)
                        .isAchieved(true)
        );

        // when
        memberService.patchRepresentativeBadge(member.getId(), badge.getId());

        // then
        assertThat(member.getRepresentativeBadge()).isEqualTo(badge);
    }

    @DisplayName("예외: 대표 뱃지가 수정이 될 때 소유하지 않은 뱃지면 예외가 발생한다")
    @Test
    void patchRepresentativeBadge_noOwnBadgeThrowNotFoundException() {
        // given
        Badge badge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        Member member = memberFactory.save(builder -> builder.nickname("우가"));

        // when & then
        assertThatThrownBy(() -> memberService.patchRepresentativeBadge(member.getId(), badge.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member does not own this badge");
    }
}
