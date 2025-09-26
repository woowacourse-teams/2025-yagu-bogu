package com.yagubogu.support;

import jakarta.persistence.EntityManager;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
public abstract class E2eTestBase {

    protected static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("yagubogu_test_db_mysql")
            .withUsername("yagu")
            .withPassword("bogu")
            .waitingFor(
                    org.testcontainers.containers.wait.strategy.Wait
                            .forLogMessage(".*ready for connections.*", 2)
                            .withStartupTimeout(java.time.Duration.ofMinutes(2))
            )
            .withStartupTimeout(java.time.Duration.ofMinutes(3))
            .withReuse(true);

    static {
        // Start container once for the whole test JVM
        mysql.start();
    }

    @Autowired
    private EntityManager em;
    @Autowired
    private TransactionTemplate txTemplate;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeAll
    static void migrateOnce(@Autowired Flyway flyway) {
        flyway.migrate(); // DDL 한 번만 실행
    }

    @AfterEach
    void cleanData() {
        txTemplate.executeWithoutResult(status -> {
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

            // Keep lookup tables (teams, stadiums) seeded by Flyway
            em.createNativeQuery("TRUNCATE TABLE members").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE games").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE refresh_tokens").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE talk_reports").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE check_ins").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE talks").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE likes").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE like_windows").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE member_badges").executeUpdate();

            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        });
    }
}
