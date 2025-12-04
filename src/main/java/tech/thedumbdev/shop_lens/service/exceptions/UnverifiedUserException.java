package tech.thedumbdev.shop_lens.service.exceptions;

public class UnverifiedUserException extends UserException {
    public UnverifiedUserException(String message) {
        super(message);
    }
    public UnverifiedUserException(String message, Throwable cause) { super(message, cause); }
}
