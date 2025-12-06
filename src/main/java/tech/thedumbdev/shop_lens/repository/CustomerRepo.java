package tech.thedumbdev.shop_lens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.Customer;
import tech.thedumbdev.shop_lens.model.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepo extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByShopifyIdAndTenant(String shopifyId, Tenant tenant);
}
