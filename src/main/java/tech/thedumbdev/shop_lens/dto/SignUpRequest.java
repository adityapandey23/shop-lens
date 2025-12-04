package tech.thedumbdev.shop_lens.dto;

public record SignUpRequest(
    String firstName,
    String lastName,
    String email,
    String password
) {}
