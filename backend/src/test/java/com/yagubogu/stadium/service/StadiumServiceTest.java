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
                new StadiumResponse(1L, "잠실 야구장"),
                new StadiumResponse(2L, "고척 스카이돔"),
                new StadiumResponse(3L, "인천 SSG 랜더스필드"),
                new StadiumResponse(4L, "대전 한화생명 볼파크"),
                new StadiumResponse(5L, "광주 KIA 챔피언스필드"),
                new StadiumResponse(6L, "대구 삼성라이온즈파크"),
                new StadiumResponse(7L, "창원 NC파크"),
                new StadiumResponse(8L, "수원 KT위즈파크"),
                new StadiumResponse(9L, "부산 사직야구장")
        );

        // when
        StadiumsResponse actual = stadiumService.findAll();
        
        // then
        Assertions.assertThat(actual.stadiums()).isEqualTo(expected);
    }
}
