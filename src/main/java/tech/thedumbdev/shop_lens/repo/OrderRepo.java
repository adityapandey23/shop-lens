package tech.thedumbdev.shop_lens.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.Order;

import java.util.UUID;

public interface OrderRepo extends JpaRepository<Order, UUID> {
}
