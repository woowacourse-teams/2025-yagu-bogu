package com.yagubogu.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * RabbitMQ 설정 (Backend 모듈 - Consumer)
 *
 * 크롤링 서버로부터 경기 종료 이벤트를 수신
 */
@EnableRabbit
@Configuration
@Slf4j
public class RabbitMQConfig {

    public static final String GAME_FINALIZED_EXCHANGE = "game.finalized.exchange";
    public static final String GAME_FINALIZED_ROUTING_KEY = "game.finalized";
    public static final String GAME_FINALIZED_ETL_QUEUE = "game.finalized.etl.queue";
    public static final String GAME_FINALIZED_ETL_DELAY_QUEUE = "game.finalized.etl.delay.queue";
    public static final String GAME_FINALIZED_ETL_DELAY_ROUTING_KEY = "game.finalized.etl.delay";
    public static final String GAME_FINALIZED_ETL_RETRY_ROUTING_KEY = "game.finalized.etl.retry";
    public static final String GAME_FINALIZED_ETL_DLQ = "game.finalized.etl.dlq";
    public static final String GAME_FINALIZED_ETL_DLX = "game.finalized.etl.dlx";
    public static final String GAME_FINALIZED_ETL_DLQ_ROUTING_KEY = "game.finalized.etl.dlq";

    public static final String GAME_FINALIZED_STATS_ROUTING_KEY = "etl.completed";
    public static final String GAME_FINALIZED_STATS_QUEUE = "game.finalized.stats.queue";
    public static final String GAME_FINALIZED_STATS_DELAY_QUEUE = "game.finalized.stats.delay.queue";
    public static final String GAME_FINALIZED_STATS_DELAY_ROUTING_KEY = "game.finalized.stats.delay";
    public static final String GAME_FINALIZED_STATS_RETRY_ROUTING_KEY = "game.finalized.stats.retry";
    public static final String GAME_FINALIZED_STATS_DLQ = "game.finalized.stats.dlq";
    public static final String GAME_FINALIZED_STATS_DLX = "game.finalized.stats.dlx";
    public static final String GAME_FINALIZED_STATS_DLQ_ROUTING_KEY = "game.finalized.stats.dlq";

    private static final int ETL_RETRY_DELAY_MS = 5 * 1000; // 5초 대기 후 재시도
    private static final int STATS_RETRY_DELAY_MS = 5 * 1000; // 5초 대기 후 재시도

    @Bean
    public TopicExchange gameFinalizedExchange() {
        return new TopicExchange(GAME_FINALIZED_EXCHANGE, true, false);
    }

    @Bean
    public Queue gameFinalizedEtlQueue() {
        return QueueBuilder.durable(GAME_FINALIZED_ETL_QUEUE)
                .withArgument("x-dead-letter-exchange", GAME_FINALIZED_ETL_DLX)
                .withArgument("x-dead-letter-routing-key", GAME_FINALIZED_ETL_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue gameFinalizedEtlDelayQueue() {
        return QueueBuilder.durable(GAME_FINALIZED_ETL_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", GAME_FINALIZED_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", GAME_FINALIZED_ETL_RETRY_ROUTING_KEY)
                .withArgument("x-message-ttl", ETL_RETRY_DELAY_MS)
                .build();
    }

    @Bean
    public Queue gameFinalizedEtlDlq() {
        return QueueBuilder.durable(GAME_FINALIZED_ETL_DLQ).build();
    }

    @Bean
    public DirectExchange gameFinalizedEtlDlx() {
        return new DirectExchange(GAME_FINALIZED_ETL_DLX, true, false);
    }

    @Bean
    public Queue gameFinalizedStatsQueue() {
        return QueueBuilder.durable(GAME_FINALIZED_STATS_QUEUE)
                .withArgument("x-dead-letter-exchange", GAME_FINALIZED_STATS_DLX)
                .withArgument("x-dead-letter-routing-key", GAME_FINALIZED_STATS_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue gameFinalizedStatsDelayQueue() {
        return QueueBuilder.durable(GAME_FINALIZED_STATS_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", GAME_FINALIZED_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", GAME_FINALIZED_STATS_RETRY_ROUTING_KEY)
                .withArgument("x-message-ttl", STATS_RETRY_DELAY_MS)
                .build();
    }

    @Bean
    public Queue gameFinalizedStatsDlq() {
        return QueueBuilder.durable(GAME_FINALIZED_STATS_DLQ).build();
    }

    @Bean
    public DirectExchange gameFinalizedStatsDlx() {
        return new DirectExchange(GAME_FINALIZED_STATS_DLX, true, false);
    }

    @Bean
    public Binding gameFinalizedEtlBinding() {
        return BindingBuilder.bind(gameFinalizedEtlQueue())
                .to(gameFinalizedExchange())
                .with(GAME_FINALIZED_ROUTING_KEY);
    }

    @Bean
    public Binding gameFinalizedEtlRetryBinding() {
        return BindingBuilder.bind(gameFinalizedEtlQueue())
                .to(gameFinalizedExchange())
                .with(GAME_FINALIZED_ETL_RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding gameFinalizedEtlDelayBinding() {
        return BindingBuilder.bind(gameFinalizedEtlDelayQueue())
                .to(gameFinalizedExchange())
                .with(GAME_FINALIZED_ETL_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding gameFinalizedEtlDlqBinding() {
        return BindingBuilder.bind(gameFinalizedEtlDlq())
                .to(gameFinalizedEtlDlx())
                .with(GAME_FINALIZED_ETL_DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding gameFinalizedStatsBinding() {
        return BindingBuilder.bind(gameFinalizedStatsQueue())
                .to(gameFinalizedExchange())
                .with(GAME_FINALIZED_STATS_ROUTING_KEY);
    }

    @Bean
    public Binding gameFinalizedStatsRetryBinding() {
        return BindingBuilder.bind(gameFinalizedStatsQueue())
                .to(gameFinalizedExchange())
                .with(GAME_FINALIZED_STATS_RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding gameFinalizedStatsDelayBinding() {
        return BindingBuilder.bind(gameFinalizedStatsDelayQueue())
                .to(gameFinalizedExchange())
                .with(GAME_FINALIZED_STATS_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding gameFinalizedStatsDlqBinding() {
        return BindingBuilder.bind(gameFinalizedStatsDlq())
                .to(gameFinalizedStatsDlx())
                .with(GAME_FINALIZED_STATS_DLQ_ROUTING_KEY);
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
        if (connectionFactory instanceof CachingConnectionFactory cachingConnectionFactory) {
            cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
            cachingConnectionFactory.setPublisherReturns(true);
        }

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        template.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        template.setConfirmCallback(this::handlePublishConfirm);
        template.setReturnsCallback(returned -> log.error(
                "[RABBITMQ] Returned message from exchange={} routingKey={} replyCode={} replyText={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText()));
        return template;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void declareRabbitTopology(ApplicationReadyEvent event) {
        log.info("Declaring RabbitMQ topology on ApplicationReadyEvent");

        AmqpAdmin amqpAdmin = event.getApplicationContext().getBean(AmqpAdmin.class);

        amqpAdmin.declareExchange(gameFinalizedExchange());
        amqpAdmin.declareExchange(gameFinalizedEtlDlx());
        amqpAdmin.declareExchange(gameFinalizedStatsDlx());

        amqpAdmin.declareQueue(gameFinalizedEtlQueue());
        amqpAdmin.declareQueue(gameFinalizedEtlDelayQueue());
        amqpAdmin.declareQueue(gameFinalizedEtlDlq());
        amqpAdmin.declareQueue(gameFinalizedStatsQueue());
        amqpAdmin.declareQueue(gameFinalizedStatsDelayQueue());
        amqpAdmin.declareQueue(gameFinalizedStatsDlq());

        amqpAdmin.declareBinding(gameFinalizedEtlBinding());
        amqpAdmin.declareBinding(gameFinalizedEtlRetryBinding());
        amqpAdmin.declareBinding(gameFinalizedEtlDelayBinding());
        amqpAdmin.declareBinding(gameFinalizedEtlDlqBinding());
        amqpAdmin.declareBinding(gameFinalizedStatsBinding());
        amqpAdmin.declareBinding(gameFinalizedStatsRetryBinding());
        amqpAdmin.declareBinding(gameFinalizedStatsDelayBinding());
        amqpAdmin.declareBinding(gameFinalizedStatsDlqBinding());
    }

    private void handlePublishConfirm(CorrelationData correlationData, boolean ack, String cause) {
        if (!ack) {
            log.error("[RABBITMQ] Publish confirm failed: correlationId={}, cause={}",
                    correlationData != null ? correlationData.getId() : "null", cause);
        }
    }
}
