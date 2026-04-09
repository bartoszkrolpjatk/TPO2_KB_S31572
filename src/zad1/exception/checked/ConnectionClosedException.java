package zad1.exception.checked;

public class ConnectionClosedException extends SimpleChatCheckedException {
    public ConnectionClosedException(String message) {
        super(message);
    }
}
