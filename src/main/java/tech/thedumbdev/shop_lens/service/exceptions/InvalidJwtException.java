package tech.thedumbdev.shop_lens.service.exceptions;

public class InvalidJwtException extends JwtException {
    public InvalidJwtException(String message) {
        super(message);
    }
    public InvalidJwtException(String message, Throwable cause) { super(message, cause); }
}
