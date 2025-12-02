package tech.thedumbdev.shop_lens.service;

public interface OtpService {
    public String generateAndStoreOtp(String email);
    public boolean validateOtp(String email, String inputOtp);
}
