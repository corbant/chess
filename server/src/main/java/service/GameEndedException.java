package service;

public class GameEndedException extends Exception {
    public GameEndedException(String message) {
        super(message);
    }

    public GameEndedException(String message, Throwable ex) {
        super(message, ex);
    }
}
