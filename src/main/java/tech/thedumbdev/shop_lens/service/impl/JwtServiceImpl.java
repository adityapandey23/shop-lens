package tech.thedumbdev.shop_lens.service.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.thedumbdev.shop_lens.model.RefreshToken;
import tech.thedumbdev.shop_lens.model.User;
import tech.thedumbdev.shop_lens.model.enums.TokenType;
import tech.thedumbdev.shop_lens.repository.RefreshTokenRepo;
import tech.thedumbdev.shop_lens.service.JwtService;
import tech.thedumbdev.shop_lens.util.HashUtil;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${REFRESH_TOKEN_TTL:604800000}")
    private long refreshTokenTTL;

    private final RefreshTokenRepo refreshTokenRepo;
    private final HashUtil hashUtil;

    JwtServiceImpl(
            RefreshTokenRepo refreshTokenRepo,
            HashUtil hashUtil
    ) {
        this.refreshTokenRepo = refreshTokenRepo;
        this.hashUtil = hashUtil;
    }

    @Override
    public String generateToken(User user, TokenType tokenType, long duration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", tokenType.toString());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + duration))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean verifyToken(String token, TokenType requiredType) {
        try {
            Jws<Claims> claimsJwt = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            String tokenType = claimsJwt.getPayload().get("tokenType").toString();

            return requiredType.toString().equals(tokenType);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String generateRefreshToken(final User user) {
        // Generating a new JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", TokenType.REFRESH_TOKEN.toString());
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + refreshTokenTTL);

        String token = Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();

        // Saving in the database
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(expiration.toInstant());
        refreshToken.setTokenHash(hashUtil.Hasher(token));
        refreshTokenRepo.save(refreshToken);

        return token;
    }

    @Override
    public boolean verifyRefreshToken(final String refreshToken) {
        // Cheap check
        if(!verifyToken(refreshToken, TokenType.REFRESH_TOKEN)) {
            return false;
        }

        // Expensive check
        String tokenHash = hashUtil.Hasher(refreshToken);
        return refreshTokenRepo.findByTokenHash(tokenHash)
                .map(token -> {
                    // Double check DB expiry (though JWT verifyToken covers this usually)
                    if (token.getExpiresAt().isBefore(Instant.now())) {
                        return false;
                    }
                    // Valid and exists in DB
                    return true;
                })
                .orElse(false); // Token not found in DB (Revoked)
    }

    @Override
    public UUID extractUserId(String token) {
        String userIdString = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return UUID.fromString(userIdString);
    }

    // Helper function
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
