package tech.thedumbdev.shop_lens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.Tenant;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepo extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByShopDomain(String shopDomain);
}
