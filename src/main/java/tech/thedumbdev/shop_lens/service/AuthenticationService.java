package tech.thedumbdev.shop_lens.service;

import tech.thedumbdev.shop_lens.dto.AuthResponse;
import tech.thedumbdev.shop_lens.dto.SignInRequest;
import tech.thedumbdev.shop_lens.dto.SignUpRequest;
import tech.thedumbdev.shop_lens.dto.VerifyEmailRequest;

public interface AuthenticationService {
    public AuthResponse signUp(SignUpRequest request);
    public AuthResponse signIn(SignInRequest request);
    public AuthResponse verifyEmail(VerifyEmailRequest request);
    public AuthResponse sendVerification(String email);
}