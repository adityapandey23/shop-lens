package tech.thedumbdev.shop_lens.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    // Normal Queue
    public static final String SHOPIFY_SYNC_QUEUE = "shopify_sync_queue";
    public static final String SHOPIFY_SYNC_EXCHANGE = "shopify_sync_exchange";
    public static final String SHOPIFY_ROUTING_KEY = "shopify_routing_key";

    // Dead Letter Queue for failed messages
    public static final String SHOPIFY_DLQ = "shopify_sync_dlq";
    public static final String SHOPIFY_DLQ_EXCHANGE = "shopify_sync_dlq_exchange";
    public static final String SHOPIFY_DLQ_ROUTING_KEY = "shopify_dlq_routing_key";

    @Bean
    public Queue shopifySyncQueue() {
        return QueueBuilder.durable(SHOPIFY_SYNC_QUEUE)
                .withArgument("x-dead-letter-exchange", SHOPIFY_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SHOPIFY_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(SHOPIFY_DLQ).build();
    }

    @Bean
    public DirectExchange shopifySyncExchange() {
        return new DirectExchange(SHOPIFY_SYNC_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(SHOPIFY_DLQ_EXCHANGE);
    }

    @Bean
    public Binding shopifySyncBinding(Queue shopifySyncQueue, DirectExchange shopifySyncExchange) {
        return BindingBuilder.bind(shopifySyncQueue)
                .to(shopifySyncExchange)
                .with(SHOPIFY_ROUTING_KEY);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(SHOPIFY_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
