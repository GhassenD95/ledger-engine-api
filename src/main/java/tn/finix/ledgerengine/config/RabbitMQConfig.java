package tn.finix.ledgerengine.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "ledger.events.exchange";
    public static final String QUEUE_NAME = "ledger.transfers.queue";
    public static final String ROUTING_KEY = "transfer.completed";

    @Bean
    public TopicExchange ledgerExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue transferQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding transferBinding(Queue transferQueue, TopicExchange ledgerExchange) {
        return BindingBuilder.bind(transferQueue).to(ledgerExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
