package zad1;

abstract class SimpleChatException extends RuntimeException {

    SimpleChatException(String message, Throwable cause) {
        super(message, cause);
    }

    SimpleChatException(String message) {
        super(message);
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

    static class ClientCannotConnect extends SimpleChatException {
        ClientCannotConnect(Throwable cause) {
            super("Exception while connecting to server! %s".formatted(cause.getMessage()), cause);
        }
    }

    static class ListeningToBroadcastFailed extends SimpleChatException {
        ListeningToBroadcastFailed(String message) {
            super(message);
        }

        ListeningToBroadcastFailed(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class CloseFailed extends SimpleChatException {
        CloseFailed(Throwable cause) {
            super("Exception while closing channel and selector %s".formatted(cause.getMessage()), cause);
        }
    }

    static class UnsupportedOperation extends SimpleChatException {
        UnsupportedOperation(String unsupportedOperation) {
            super("Unsupported operation: %s".formatted(unsupportedOperation));
        }
    }
}
