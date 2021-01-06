package exception;

public class WrongDataGivenException extends Exception {
    public WrongDataGivenException(String message) { super(message); }
    public WrongDataGivenException() { super(); }
    public WrongDataGivenException(String message, Exception cause) { super(message, cause); }
}
