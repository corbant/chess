package client;

public class AlreadyTakenException extends Exception {

    public AlreadyTakenException(String message) {
        super(message);
    }

    public AlreadyTakenException(Throwable cause) {
        super(cause);
    }

    public AlreadyTakenException(String message, Throwable cause) {
        super(message, cause);
    }

}
