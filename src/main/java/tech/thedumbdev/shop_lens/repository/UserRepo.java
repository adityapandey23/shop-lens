package tech.thedumbdev.shop_lens.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    public Optional<User> findByEmail(String email);
}
