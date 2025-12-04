package tech.thedumbdev.shop_lens.service.exceptions;

public class DuplicateUserException extends UserException {
    public DuplicateUserException(String message) { super(message); }

    public DuplicateUserException(String message, Throwable cause) { super(message, cause); }
}
