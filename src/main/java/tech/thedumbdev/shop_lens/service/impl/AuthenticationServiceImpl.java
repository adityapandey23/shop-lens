package tech.thedumbdev.shop_lens.service.impl;

import tech.thedumbdev.shop_lens.dto.AuthResponse;
import tech.thedumbdev.shop_lens.dto.SignInRequest;
import tech.thedumbdev.shop_lens.dto.SignUpRequest;
import tech.thedumbdev.shop_lens.dto.VerifyEmailRequest;
import tech.thedumbdev.shop_lens.model.User;
import tech.thedumbdev.shop_lens.repository.UserRepo;
import tech.thedumbdev.shop_lens.service.*;
import tech.thedumbdev.shop_lens.util.HashUtil;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepo userRepo;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final HashUtil hashUtil;

    public AuthenticationServiceImpl(
            UserRepo userRepo,
            OtpService otpService,
            EmailService emailService,
            JwtService jwtService,
            HashUtil hashUtil
    ) {
        this.userRepo = userRepo;
        this.otpService = otpService;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.hashUtil = hashUtil;
    }

    @Override
    public AuthResponse signUp(SignUpRequest request) {
        if(userRepo.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(hashUtil.Hasher(request.password()));
        user.setVerified(false);

        userRepo.save(user);

        return sendVerificationHelper(user, "OTP sent");
    }

    @Override
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        if(!jwtService.verifyVerificationToken(request.verificationToken())) {
            throw new RuntimeException("Invalid verification token");
        }

        User user = userRepo.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("User not found"));

        if(!otpService.validateOtp(user.getEmail(), request.otp())) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setVerified(true);
        userRepo.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, null, "Email verified");
    }

    @Override
    public AuthResponse signIn(SignInRequest request) {
        User user = userRepo.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("User doesn't exist"));

        String hashedPassword = hashUtil.Hasher(request.password());
        if (!user.getPassword().equals(hashedPassword)) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Account not verified"); // Do call the reverify route
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, null, "Login successful");
    }

    @Override
    public AuthResponse sendVerification(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("User already verified");
        }

        return sendVerificationHelper(user, "OTP resent");
    }

    // Helper function
    private AuthResponse sendVerificationHelper(User user, String message) {
        String otp = otpService.generateAndStoreOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp);
        String verificationToken = jwtService.generateVerificationToken(user);

        return new AuthResponse(null, null, verificationToken, message);
    }
}
