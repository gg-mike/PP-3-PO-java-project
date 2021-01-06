package exception;

public class NotInitedException extends Exception {
    public NotInitedException(String message) { super(message); }
    public NotInitedException() { super(); }
    public NotInitedException(String message, Exception cause) { super(message, cause); }
}
