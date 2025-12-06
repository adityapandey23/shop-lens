package tech.thedumbdev.shop_lens.service;

import tech.thedumbdev.shop_lens.dto.ShopifySyncMessage;

public interface QueueService {
    void publishSyncMessage(ShopifySyncMessage syncMessage);
}
