package zad1.exception.checked;

public class InvalidMessageFormatException extends SimpleChatCheckedException {
    private final String invalidFormatMessage;

    public InvalidMessageFormatException(String message, String invalidFormatMessage) {
        super(message);
        this.invalidFormatMessage = invalidFormatMessage;
    }

    public String invalidFormatMessage() {
        return invalidFormatMessage;
    }
}
