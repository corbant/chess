package client;

public class InvalidCommandException extends RuntimeException {
    InvalidCommandException(String message) {
        super(message);
    }

    InvalidCommandException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
