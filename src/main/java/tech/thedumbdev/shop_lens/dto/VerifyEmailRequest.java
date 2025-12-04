package tech.thedumbdev.shop_lens.dto;

public record VerifyEmailRequest(
    String email,
    String otp,
    String verificationToken
) {}
