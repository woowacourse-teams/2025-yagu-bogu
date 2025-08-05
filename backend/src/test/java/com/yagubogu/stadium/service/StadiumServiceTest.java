package com.yagubogu.stadium.service;

import com.yagubogu.stadium.dto.StadiumResponse;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
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
class StadiumServiceTest {

    private StadiumService stadiumService;

    @Autowired
    private StadiumRepository stadiumRepository;

    @BeforeEach
    void setUp() {
        stadiumService = new StadiumService(stadiumRepository);
    }

    @DisplayName("전체 구장 목록을 조회한다")
    @Test
    void findAllStadiums() {
        // given
        List<StadiumResponse> expected = List.of(
                new StadiumResponse(1L, "잠실 야구장", "잠실구장", "잠실", 37.512192, 127.072055),
                new StadiumResponse(2L, "고척 스카이돔", "고척돔", "고척", 37.498191, 126.867073),
                new StadiumResponse(3L, "인천 SSG 랜더스필드", "랜더스필드", "인천", 37.437196, 126.693294),
                new StadiumResponse(4L, "대전 한화생명 볼파크", "볼파크", "대전", 36.316589, 127.431211),
                new StadiumResponse(5L, "광주 KIA 챔피언스필드", "챔피언스필드", "광주", 35.168282, 126.889138),
                new StadiumResponse(6L, "대구 삼성라이온즈파크", "라이온즈파크", "대구", 35.841318, 128.681559),
                new StadiumResponse(7L, "창원 NC파크", "엔씨파크", "창원", 35.222754, 128.582251),
                new StadiumResponse(8L, "수원 KT위즈파크", "위즈파크", "수원", 37.299977, 127.009690),
                new StadiumResponse(9L, "부산 사직야구장", "사직구장", "부산", 35.194146, 129.061497)
        );

        // when
        StadiumsResponse actual = stadiumService.findAll();

        // then
        Assertions.assertThat(actual.stadiums()).isEqualTo(expected);
    }
}
