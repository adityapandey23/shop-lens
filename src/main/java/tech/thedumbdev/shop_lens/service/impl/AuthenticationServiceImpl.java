package tech.thedumbdev.shop_lens.service.impl;

import org.springframework.stereotype.Service;
import tech.thedumbdev.shop_lens.dto.AuthResponse;
import tech.thedumbdev.shop_lens.dto.SignInRequest;
import tech.thedumbdev.shop_lens.dto.SignUpRequest;
import tech.thedumbdev.shop_lens.dto.VerifyEmailRequest;
import tech.thedumbdev.shop_lens.model.User;
import tech.thedumbdev.shop_lens.repository.UserRepo;
import tech.thedumbdev.shop_lens.service.*;
import tech.thedumbdev.shop_lens.service.exceptions.*;
import tech.thedumbdev.shop_lens.util.HashUtil;

import java.util.UUID;

@Service
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
    public AuthResponse signUp(SignUpRequest request) throws UserException {
        if(userRepo.findByEmail(request.email()).isPresent()) {
            throw new DuplicateUserException("User already exists");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(hashUtil.Hasher(request.password()));
        user.setVerified(false);

        userRepo.save(user);

        return sendVerificationHelper(user);
    }

    @Override
    public AuthResponse verifyEmail(VerifyEmailRequest request) throws UserException, JwtException, OtpException {
        if(!jwtService.verifyVerificationToken(request.verificationToken())) {
            throw new InvalidJwtException("Invalid verification token");
        }

        User user = userRepo.findByEmail(request.email()).orElseThrow(() -> new NotFoundUserException("User not found"));

        if(!otpService.validateOtp(user.getEmail(), request.otp())) {
            throw new InvalidOtpException("Invalid OTP");
        }

        user.setVerified(true);
        userRepo.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, null, "Email verified");
    }

    @Override
    public AuthResponse signIn(SignInRequest request) throws UserException {
        User user = userRepo.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("User doesn't exist"));

        String hashedPassword = hashUtil.Hasher(request.password());
        if (!user.getPassword().equals(hashedPassword)) {
            throw new InvalidCredentialsUserException("Password doesn't match");
        }

        if (!user.isVerified()) {
            throw new UnverifiedUserException("Account not verified"); // Do call the reverify route
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, null, "Login successful");
    }

    @Override
    public AuthResponse sendVerification(String email) throws UserException {
        User user = userRepo.findByEmail(email).orElseThrow(() -> new NotFoundUserException("User not found"));

        if (user.isVerified()) {
            throw new DuplicateUserException("User already verified");
        }

        return sendVerificationHelper(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) throws UserException, JwtException {
        if (!jwtService.verifyRefreshToken(refreshToken)) {
            throw new InvalidJwtException("Invalid or expired refresh token");
        }

        UUID userId = jwtService.extractUserId(refreshToken);
        User user = userRepo.findById(userId).orElseThrow(() -> new NotFoundUserException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);

        return new AuthResponse(accessToken, refreshToken, null, "Refresh token verified");
    }

    // Helper function
    private AuthResponse sendVerificationHelper(User user) {
        String otp = otpService.generateAndStoreOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp);
        String verificationToken = jwtService.generateVerificationToken(user);

        return new AuthResponse(null, null, verificationToken, "OTP sent");
    }
}
