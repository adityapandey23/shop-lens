package tech.thedumbdev.shop_lens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.Order;
import tech.thedumbdev.shop_lens.model.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepo extends JpaRepository<Order, UUID> {
    Optional<Order> findByShopifyIdAndTenant(String shopifyId, Tenant tenant);
}
