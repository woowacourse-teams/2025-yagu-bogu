package yagubogu.crawling.game.service.crawler;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.event.GameFinalizedEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QuickRabbitMQTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void quickTest() throws InterruptedException {
        GameFinalizedEvent event = new GameFinalizedEvent(LocalDate.now(), "잠실",
                "LG", "KIA", LocalTime.of(6,30), GameState.CANCELED);
        // 발행
        rabbitTemplate.convertAndSend(
                "game.finalized.queue",
                event
        );

        System.out.println("발행 완료! 관리 UI에서 확인: http://localhost:15672");

        // 수동으로 관리 UI 확인
        Thread.sleep(60000);  // 1분 대기
    }
}
