package tech.thedumbdev.shop_lens.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.thedumbdev.shop_lens.repository.RefreshTokenRepo;

import java.time.Instant;

@Component
public class TokenCleanupScheduler {
    private final RefreshTokenRepo refreshTokenRepo;

    public TokenCleanupScheduler(RefreshTokenRepo refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeExpiredTokens() {
        Instant now = Instant.now();
        refreshTokenRepo.deleteAllByExpiresAtBefore(now);
        System.out.println("Cleanup: Removed expired refresh tokens at " + now.toString());
    }
}
