package com.yagubogu.game;

import java.util.Map;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class GameSchemaMigrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @DisplayName("신규 설치와 V22 업그레이드 모두 스키마 검증을 통과하고 실시간 상태를 보존한다")
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void validateGameSchema(final boolean upgradeFromV22) {
        var dataSource = new DriverManagerDataSource(
                mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        Flyway flyway = Flyway.configure().dataSource(dataSource)
                .locations("classpath:db/migration").cleanDisabled(false).load();
        flyway.clean();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        if (upgradeFromV22) {
            Flyway.configure().dataSource(dataSource).locations("classpath:db/migration")
                    .target("22").load().migrate();
            insertGame(jdbc, "live", "12, 3, 2, 1");
            insertGame(jdbc, "scheduled", "NULL, NULL, NULL, NULL");
        }

        flyway.migrate();
        flyway.validate();

        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.yagubogu");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "validate"));
        try {
            factory.afterPropertiesSet();
            assertThat(factory.getObject()).isNotNull();
        } finally {
            factory.destroy();
        }

        if (upgradeFromV22) {
            assertThat(jdbc.queryForMap("""
                    SELECT current_inning, balls, strikes, outs FROM games WHERE game_code = 'live'
                    """))
                    .containsEntry("current_inning", 12).containsEntry("balls", 3)
                    .containsEntry("strikes", 2).containsEntry("outs", 1);
            assertThat(jdbc.queryForMap("""
                    SELECT current_inning, balls, strikes, outs FROM games WHERE game_code = 'scheduled'
                    """).values()).containsOnlyNulls();
        }
    }

    private void insertGame(final JdbcTemplate jdbc, final String gameCode, final String counts) {
        jdbc.update("""
                INSERT INTO games (game_code, date, start_at, stadium_id, home_team_id, away_team_id,
                                   current_inning, balls, strikes, outs)
                SELECT ?, '2026-09-10', '18:30:00',
                       (SELECT MIN(stadium_id) FROM stadiums),
                       (SELECT MIN(team_id) FROM teams), (SELECT MAX(team_id) FROM teams),
                """ + counts, gameCode);
    }
}
