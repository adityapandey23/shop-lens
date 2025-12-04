package tech.thedumbdev.shop_lens.service.exceptions;

public class InvalidCredentialsUserException extends UserException {
    public InvalidCredentialsUserException(String message) {
        super(message);
    }
    public InvalidCredentialsUserException(String message, Throwable cause) { super(message, cause); }
}
