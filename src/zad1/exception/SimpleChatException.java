package zad1.exception;

public abstract class SimpleChatException extends RuntimeException {

    private SimpleChatException(String message, Throwable cause) {
        super(message, cause);
    }

    private SimpleChatException(String message) {
        super(message);
    }

    public static class ServerStartFailed extends SimpleChatException {
        public ServerStartFailed(Throwable cause) {
            super("Exception while starting the server! %s".formatted(cause.getMessage()), cause);
        }
    }

    public static class InternalServerError extends SimpleChatException {
        public InternalServerError(Throwable cause) {
            super("Exception while handling clients! %s".formatted(cause.getMessage()), cause);
        }
    }

    public static class ClientCannotConnect extends SimpleChatException {
        public ClientCannotConnect(Throwable cause) {
            super("Exception while connecting to server! %s".formatted(cause.getMessage()), cause);
        }
    }

    public static class ListeningToBroadcastFailed extends SimpleChatException {
        public ListeningToBroadcastFailed(String message) {
            super(message);
        }
    }

    public static class CloseFailed extends SimpleChatException {
        public CloseFailed(Throwable cause) {
            super("Exception while closing channel and selector %s".formatted(cause.getMessage()), cause);
        }
    }
}
