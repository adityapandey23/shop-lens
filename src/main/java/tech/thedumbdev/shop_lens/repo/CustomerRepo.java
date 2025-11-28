package tech.thedumbdev.shop_lens.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.Customer;

import java.util.UUID;

public interface CustomerRepo extends JpaRepository<Customer, UUID> {
}
