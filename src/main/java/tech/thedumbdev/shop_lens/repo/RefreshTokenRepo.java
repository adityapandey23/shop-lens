package tech.thedumbdev.shop_lens.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thedumbdev.shop_lens.model.RefreshToken;
import tech.thedumbdev.shop_lens.model.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken,  UUID> {
    // Getting the token
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // For the cleanup job
    void deleteAllByExpiresAtBefore(Instant now);

    // For logout from all devices functionality
    void deleteByUser_Id(UUID userId);
}
