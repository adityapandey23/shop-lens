package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tech.thedumbdev.shop_lens.config.RabbitMQConfig;
import tech.thedumbdev.shop_lens.dto.ShopifySyncMessage;
import tech.thedumbdev.shop_lens.service.QueueService;

@Service
public class QueueServiceImpl implements QueueService {

    private final RabbitTemplate rabbitTemplate;

    public QueueServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishSyncMessage(ShopifySyncMessage syncMessage) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SHOPIFY_SYNC_EXCHANGE,
                RabbitMQConfig.SHOPIFY_ROUTING_KEY,
                syncMessage
        );
    }
}
