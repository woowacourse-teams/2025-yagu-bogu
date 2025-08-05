package com.yagubogu.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.repository.MemberRepository;
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

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
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

    @DisplayName("예외: 멤버를 찾지 못하면 예외가 발생한다.")
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
}
