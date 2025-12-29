package yagubogu.crawling.game.config;

import com.yagubogu.global.config.RabbitMQConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정 (Crawler 모듈 - Producer)
 *
 * 크롤링 서버에서 전송하는 메시지를 JSON으로 직렬화하여
 * Backend의 Consumer(Jackson2JsonMessageConverter)와 호환되도록 설정
 */
@Configuration
public class RabbitProducerConfig {

    @Bean
    public TopicExchange gameFinalizedExchange() {
        return new TopicExchange(RabbitMQConfig.GAME_FINALIZED_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            final ConnectionFactory connectionFactory,
            final MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
