package dataaccess;

/**
 * Indicates a user made a request without the proper auth token
 */
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable ex) {
        super(message, ex);
    }
}