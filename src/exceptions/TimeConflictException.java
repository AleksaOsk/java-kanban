package exceptions;

public class TimeConflictException extends RuntimeException {
    public TimeConflictException(String message) {
        super(message);
    }

    public TimeConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}