package client;

public class ConnectionErrorException extends Exception {

    public ConnectionErrorException(String message) {
        super(message);
    }

    public ConnectionErrorException(Throwable cause) {
        super(cause);
    }

    public ConnectionErrorException(String message, Throwable cause) {
        super(message, cause);
    }

}
