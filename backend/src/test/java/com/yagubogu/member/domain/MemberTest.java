package com.yagubogu.member.domain;

import com.yagubogu.team.domain.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @DisplayName("어드민인지 검증한다.")
    @CsvSource({"ADMIN,true", "USER, false"})
    @ParameterizedTest
    public void validateAdmin(Role role, boolean expected) {
        //given
        Team team = new Team("기아 타이거즈", "기아", "HT");
        Member member = new Member(team, "김도영", "email", OAuthProvider.GOOGLE, "sub",
                role, "picture", null);

        //when & then
        assertThat(member.isAdmin()).isEqualTo(expected);
    }
}
