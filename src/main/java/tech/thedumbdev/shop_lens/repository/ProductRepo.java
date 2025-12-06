package tech.thedumbdev.shop_lens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.Product;
import tech.thedumbdev.shop_lens.model.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepo extends JpaRepository<Product, UUID> {
    Optional<Product> findByShopifyIdAndTenant(String shopifyId, Tenant tenant);
}
