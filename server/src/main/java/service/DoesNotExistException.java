package service;

public class DoesNotExistException extends Exception {
    public DoesNotExistException(String message) {
        super(message);
    }

    public DoesNotExistException(String message, Throwable ex) {
        super(message, ex);
    }

}
