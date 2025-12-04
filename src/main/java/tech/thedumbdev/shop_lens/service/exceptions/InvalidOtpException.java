package tech.thedumbdev.shop_lens.service.exceptions;

public class InvalidOtpException extends OtpException {
    public InvalidOtpException(String message) {
        super(message);
    }
    public InvalidOtpException(String message, Throwable cause) { super(message, cause); }
}
