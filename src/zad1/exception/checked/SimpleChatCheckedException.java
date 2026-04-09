package zad1.exception.checked;

abstract class SimpleChatCheckedException extends Exception {

    protected SimpleChatCheckedException(String message) {
        super(message);
    }
}
