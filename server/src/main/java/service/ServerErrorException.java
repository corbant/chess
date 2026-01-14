package service;

public class ServerErrorException extends Exception {
    public ServerErrorException(String message) {
        super(message);
    }

    public ServerErrorException(String message, Throwable ex) {
        super(message, ex);
    }
}
