package tech.thedumbdev.shop_lens.service;

import io.jsonwebtoken.JwtException;
import tech.thedumbdev.shop_lens.dto.AuthResponse;
import tech.thedumbdev.shop_lens.dto.SignInRequest;
import tech.thedumbdev.shop_lens.dto.SignUpRequest;
import tech.thedumbdev.shop_lens.dto.VerifyEmailRequest;
import tech.thedumbdev.shop_lens.service.exceptions.OtpException;
import tech.thedumbdev.shop_lens.service.exceptions.UserException;

public interface AuthenticationService {
    public AuthResponse signUp(SignUpRequest request) throws UserException;
    public AuthResponse signIn(SignInRequest request) throws UserException;
    public AuthResponse verifyEmail(VerifyEmailRequest request) throws UserException, JwtException, OtpException;
    public AuthResponse sendVerification(String email) throws UserException;
    public AuthResponse refreshToken(String refreshToken) throws UserException, JwtException;
}