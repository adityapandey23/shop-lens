package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import tech.thedumbdev.shop_lens.service.OtpService;

import java.time.Duration;
import java.util.Random;

public class OtpServiceImpl implements OtpService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final long OTP_TTL_MINUTES = 5;

    public OtpServiceImpl(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateAndStoreOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        String key = "otp:" + email;

        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(OTP_TTL_MINUTES)).block();

        return otp;
    }

    @Override
    public boolean validateOtp(String email, String inputOtp) {
        String key = "otp:" + email;
        String storedOtp = redisTemplate.opsForValue().get(key).block();

        if(storedOtp != null && storedOtp.equals(inputOtp)) {
            redisTemplate.delete(key).block();
            return true;
        }

        return false;
    }
}
