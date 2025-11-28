package tech.thedumbdev.shop_lens.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.OrderLineItem;

import java.util.UUID;

public interface OrderLineItemRepo extends JpaRepository<OrderLineItem, UUID> {
}
