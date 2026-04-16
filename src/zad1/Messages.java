package zad1;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class Messages {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final String serverLog;
    private final String broadcastMessage;

    Messages(String id, String message) {
        this.broadcastMessage = format(id, message);
        this.serverLog = enrichWithData(broadcastMessage);
    }

    private String enrichWithData(String message) {
        return "%s %s".formatted(LocalTime.now().format(FORMATTER), message);
    }

    private String format(String id, String message) {
        return "%s %s".formatted(id, message);
    }

    public String serverLog() {
        return serverLog;
    }

    public String broadcastMessage() {
        return broadcastMessage;
    }
}
