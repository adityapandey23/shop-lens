package tech.thedumbdev.shop_lens.service.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import tech.thedumbdev.shop_lens.model.User;
import tech.thedumbdev.shop_lens.model.enums.TokenType;
import tech.thedumbdev.shop_lens.service.JwtService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtServiceImpl implements JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

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

    // Helper function
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
