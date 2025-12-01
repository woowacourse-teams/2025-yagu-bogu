package com.yagubogu.global.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 설정 (Backend 모듈 - Consumer)
 *
 * 크롤링 서버로부터 경기 종료 이벤트를 수신
 */
@EnableRabbit
@Configuration
public class RabbitMQConfig {

    public static final String GAME_FINALIZED_QUEUE = "game.finalized.queue";

    /**
     * 경기 종료 이벤트 큐 선언
     */
    @Bean
    public Queue gameFinalizedQueue() {
        return new Queue(GAME_FINALIZED_QUEUE, true);
    }

    /**
     * JSON 메시지 컨버터
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
