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
import com.yagubogu.member.dto.v1.MemberCheckInResponse;
import com.yagubogu.member.dto.v1.MemberFavoriteRequest;
import com.yagubogu.member.dto.v1.MemberFavoriteResponse;
import com.yagubogu.member.dto.v1.MemberInfoResponse;
import com.yagubogu.member.dto.v1.MemberNicknameRequest;
import com.yagubogu.member.dto.v1.MemberNicknameResponse;
import com.yagubogu.member.dto.v1.MemberProfileBadgeResponse;
import com.yagubogu.member.dto.v1.MemberProfileResponse;
import com.yagubogu.member.dto.v1.VictoryFairyProfileResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stat.dto.CheckInSummaryParam;
import com.yagubogu.stat.dto.VictoryFairySummaryParam;
import com.yagubogu.stat.service.StatService;
import com.yagubogu.support.badge.MemberBadgeFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
public class MemberServiceTest {

    private MemberService memberService;

    @Mock
    private StatService statService;

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

    @Autowired
    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, teamRepository, badgeRepository, memberBadgeRepository,
                publisher, statService);
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
                        100.0, 100.0,
                        "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/leadoff_500.png"
                ),
                new BadgeResponseWithRates(
                        2L, "말문이 트이다", "첫 현장톡 작성",
                        Policy.CHAT, false, null,
                        0.0, 0.0,
                        "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/open_mouth_500.png"
                ),
                new BadgeResponseWithRates(
                        3L, "공포의 주둥아리", "현장톡 누적 100회",
                        Policy.CHAT, false, null,
                        0.0, 0.0,
                        "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/terrible_mouth_500.png"
                ),
                new BadgeResponseWithRates(
                        4L, "플레이볼", "첫 직관 인증",
                        Policy.CHECK_IN, false, null,
                        0.0, 0.0,
                        "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/playball_500.png"
                ),
                new BadgeResponseWithRates(
                        5L, "그랜드슬램", "9개 전구장 방문",
                        Policy.GRAND_SLAM, false, null,
                        0.0, 0.0,
                        "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/yagubogu/images/badges/500x500/grandslam_500.png"
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

    @DisplayName("사용자의 프로필 정보를 조회한다")
    @Test
    void findMemberProfile() {
        // given
        Team favoriteTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        Badge badge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        Member me = memberFactory.save(builder -> builder.nickname("진짜우가")
                .team(favoriteTeam)
                .build()
        );
        Member profileOwneredMember = memberFactory.save(builder -> builder.nickname("우가")
                .team(favoriteTeam)
                .representativeBadge(badge)
                .build()
        );
        CheckInSummaryParam fakeSummary = new CheckInSummaryParam(14, 75.0, 9, 0, 4, LocalDate.of(2025, 7, 24));
        when(statService.findCheckInSummary(anyLong(), anyInt())).thenReturn(fakeSummary);
        VictoryFairySummaryParam fakeVictorySummary = new VictoryFairySummaryParam(5L, 1L, 90.0);
        when(statService.findVictoryFairySummary(anyLong(), anyInt())).thenReturn(fakeVictorySummary);
        MemberProfileBadgeResponse expectedBadgeResponse = MemberProfileBadgeResponse.from(
                profileOwneredMember.getRepresentativeBadge());
        VictoryFairyProfileResponse expectedVictoryFairyProfileResponse = VictoryFairyProfileResponse.from(
                fakeVictorySummary);
        MemberCheckInResponse expectedCheckInResponse = MemberCheckInResponse.from(fakeSummary);

        // when
        MemberProfileResponse actual = memberService.findMemberProfile(me.getId(), profileOwneredMember.getId());

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.nickname()).isEqualTo(profileOwneredMember.getNickname().getValue());
            softAssertions.assertThat(actual.favoriteTeam()).isEqualTo(profileOwneredMember.getTeam().getShortName());
            softAssertions.assertThat(actual.profileImageUrl()).isEqualTo(profileOwneredMember.getImageUrl());
            softAssertions.assertThat(actual.enterDate()).isEqualTo(profileOwneredMember.getCreatedAt().toLocalDate());
            softAssertions.assertThat(actual.representativeBadge().imageUrl())
                    .isEqualTo(expectedBadgeResponse.imageUrl());
            softAssertions.assertThat(actual.victoryFairy().ranking())
                    .isEqualTo(expectedVictoryFairyProfileResponse.ranking());
            softAssertions.assertThat(actual.victoryFairy().rankWithinTeam())
                    .isEqualTo(expectedVictoryFairyProfileResponse.rankWithinTeam());
            softAssertions.assertThat(actual.victoryFairy().score())
                    .isEqualTo(expectedVictoryFairyProfileResponse.score());
            softAssertions.assertThat(actual.checkIn().counts()).isEqualTo(expectedCheckInResponse.counts());
            softAssertions.assertThat(actual.checkIn().winRate()).isEqualTo(expectedCheckInResponse.winRate());
            softAssertions.assertThat(actual.checkIn().winCounts()).isEqualTo(expectedCheckInResponse.winCounts());
            softAssertions.assertThat(actual.checkIn().drawCounts()).isEqualTo(expectedCheckInResponse.drawCounts());
            softAssertions.assertThat(actual.checkIn().loseCounts()).isEqualTo(expectedCheckInResponse.loseCounts());
            softAssertions.assertThat(actual.checkIn().recentCheckInDate())
                    .isEqualTo(expectedCheckInResponse.recentCheckInDate());
        });
    }

    @DisplayName("예외: 로그인한 회원을 찾을 수 없으면 예외가 발생한다")
    @Test
    void findMemberProfile_notFoundLoginMember() {
        // given
        Team favoriteTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        long invalidLoginMemberId = 999999L;
        Member profileOwneredMember = memberFactory.save(builder -> builder.nickname("우가")
                .team(favoriteTeam)
                .build()
        );

        // when & then
        assertThatThrownBy(
                () -> memberService.findMemberProfile(invalidLoginMemberId, profileOwneredMember.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }

    @DisplayName("예외: 프로필 소유자의 회원을 찾을 수 없으면 예외가 발생한다")
    @Test
    void findProfileInformation_notFoundMemberProfileOwnerMember() {
        // given
        Team favoriteTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        long invalidProfileOwnerMemberId = 999999L;
        Member me = memberFactory.save(builder -> builder.nickname("우가")
                .team(favoriteTeam)
                .build()
        );

        // when & then
        assertThatThrownBy(
                () -> memberService.findMemberProfile(me.getId(), invalidProfileOwnerMemberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }
}
