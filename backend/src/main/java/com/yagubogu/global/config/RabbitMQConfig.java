package com.yagubogu.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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

    public static final String GAME_FINALIZED_EXCHANGE = "game.finalized.exchange";
    public static final String GAME_FINALIZED_ROUTING_KEY = "game.finalized";
    public static final String GAME_FINALIZED_ETL_QUEUE = "game.finalized.etl.queue";
    public static final String GAME_FINALIZED_STATS_QUEUE = "game.finalized.stats.queue";

    @Bean
    public TopicExchange gameFinalizedExchange() {
        return new TopicExchange(GAME_FINALIZED_EXCHANGE, true, false);
    }

    @Bean
    public Queue gameFinalizedEtlQueue() {
        return new Queue(GAME_FINALIZED_ETL_QUEUE, true);
    }

    @Bean
    public Queue gameFinalizedStatsQueue() {
        return new Queue(GAME_FINALIZED_STATS_QUEUE, true);
    }

    @Bean
    public Binding gameFinalizedEtlBinding() {
        return BindingBuilder.bind(gameFinalizedEtlQueue())
                .to(gameFinalizedExchange())
                .with(GAME_FINALIZED_ROUTING_KEY);
    }

    @Bean
    public Binding gameFinalizedStatsBinding() {
        return BindingBuilder.bind(gameFinalizedStatsQueue())
                .to(gameFinalizedExchange())
                .with(GAME_FINALIZED_ROUTING_KEY);
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
