package com.yagubogu.stadium.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.stadium.dto.StadiumParam;
import com.yagubogu.stadium.dto.v1.StadiumsResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(AuthTestConfig.class)
@DataJpaTest
class StadiumServiceTest {

    private StadiumService stadiumService;

    @Autowired
    private StadiumRepository stadiumRepository;

    @BeforeEach
    void setUp() {
        stadiumService = new StadiumService(stadiumRepository);
    }

    @DisplayName("제 2 구장을 제외한 전체 구장 목록을 조회한다")
    @Test
    void findAllMainStadiumsStadiums() {
        // given
        List<StadiumParam> expected = List.of(
                new StadiumParam(1L, "광주 기아 챔피언스필드", "챔피언스필드", "광주", 35.168139, 126.889111),
                new StadiumParam(2L, "잠실 야구장", "잠실구장", "잠실", 37.512150, 127.071976),
                new StadiumParam(3L, "고척 스카이돔", "고척돔", "고척", 37.498222, 126.867250),
                new StadiumParam(4L, "수원 KT 위즈파크", "위즈파크", "수원", 37.299759, 127.009781),
                new StadiumParam(5L, "대구 삼성 라이온즈파크", "라이온즈파크", "대구", 35.841111, 128.681667),
                new StadiumParam(6L, "사직야구장", "사직구장", "사직", 35.194077, 129.061584),
                new StadiumParam(7L, "인천 SSG 랜더스필드", "랜더스필드", "문학", 37.436778, 126.693306),
                new StadiumParam(8L, "창원 NC 파크", "엔씨파크", "창원", 35.222754, 128.582251),
                new StadiumParam(9L, "대전 한화생명 볼파크", "볼파크", "대전", 36.316589, 127.431211)
        );

        // when
        StadiumsResponse actual = stadiumService.findAllMainStadiums();

        // then
        Assertions.assertThat(actual.stadiums()).isEqualTo(expected);
    }
}
