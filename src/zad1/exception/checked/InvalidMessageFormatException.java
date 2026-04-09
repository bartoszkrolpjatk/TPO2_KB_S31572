package zad1.exception.checked;

public class InvalidMessageFormatException extends SimpleChatCheckedException {
    public InvalidMessageFormatException(String message) {
        super(message);
    }
}
