package tech.thedumbdev.shop_lens.service.exceptions;

public class NotFoundUserException extends UserException{
    public NotFoundUserException(String message) {
        super(message);
    }
    public NotFoundUserException(String message, Throwable cause) { super(message, cause); }
}
