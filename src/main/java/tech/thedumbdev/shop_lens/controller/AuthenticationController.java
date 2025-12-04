package tech.thedumbdev.shop_lens.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.thedumbdev.shop_lens.dto.*;
import tech.thedumbdev.shop_lens.service.AuthenticationService;
import tech.thedumbdev.shop_lens.service.exceptions.JwtException;
import tech.thedumbdev.shop_lens.service.exceptions.OtpException;
import tech.thedumbdev.shop_lens.service.exceptions.UserException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest request) throws UserException {
        return ResponseEntity.ok(this.authenticationService.signUp(request));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@RequestBody SignInRequest request) throws UserException {
        return ResponseEntity.ok(this.authenticationService.signIn(request));
    }

    @PostMapping("/email-verify")
    public ResponseEntity<AuthResponse> emailVerify(@RequestBody VerifyEmailRequest request) throws UserException, JwtException, OtpException {
        return ResponseEntity.ok(this.authenticationService.verifyEmail(request));
    }

    @PostMapping("/resend-email-verify")
    public ResponseEntity<AuthResponse> resendEmailVerify(@RequestBody ResendEmailVerifyRequest request) throws UserException {
        return ResponseEntity.ok(this.authenticationService.sendVerification(request.email()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) throws UserException, JwtException {
        return ResponseEntity.ok(this.authenticationService.refreshToken(request.refreshToken()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");
        body.put("service", "auth");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }

}
