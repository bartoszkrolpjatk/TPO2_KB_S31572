package zad1;

abstract class SimpleChatException extends RuntimeException {

    SimpleChatException(String message, Throwable cause) {
        super(message, cause);
    }

    static class ServerStartFailed extends SimpleChatException {
        ServerStartFailed(Throwable cause) {
            super("Exception while starting the server! %s".formatted(cause.getMessage()), cause);
        }
    }

    static class InternalServerError extends SimpleChatException {
        InternalServerError(Throwable cause) {
            super("Exception while handling clients! %s".formatted(cause.getMessage()), cause);
        }
    }
}
