package tech.thedumbdev.shop_lens.service;

import tech.thedumbdev.shop_lens.model.User;
import tech.thedumbdev.shop_lens.model.enums.TokenType;

public interface JwtService {

    public String generateToken(User user, TokenType tokenType, long duration);

    public boolean verifyToken(String token, TokenType requiredToken);

    public default String generateVerificationToken(final User user) {
        return generateToken(user, TokenType.VERIFICATION_TOKEN, 1000 * 60 * 10); // 10 mins
    }

    public default String generateAccessToken(final User user) {
        return generateToken(user, TokenType.ACCESS_TOKEN, 1000 * 60 * 60); // 60 mins
    }

    public default boolean verifyVerificationToken(final String verificationToken) {
        return verifyToken(verificationToken, TokenType.VERIFICATION_TOKEN);
    }

    public default boolean verifyAccessToken(final String accessToken) {
        return verifyToken(accessToken, TokenType.ACCESS_TOKEN);
    }
}
