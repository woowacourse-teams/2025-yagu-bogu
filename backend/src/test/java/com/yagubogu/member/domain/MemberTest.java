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
        Member member = new Member(new Team("기아 타이거즈", "기아"), "김도영", role);

        //when & then
        assertThat(member.isAdmin()).isEqualTo(expected);
    }
}
