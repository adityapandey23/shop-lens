package tech.thedumbdev.shop_lens.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String verificationToken,
        String message
) {}
