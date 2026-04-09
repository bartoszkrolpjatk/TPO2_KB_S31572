package zad1;

import java.util.Arrays;

public enum Operation {
    LOGIN,
    LOGOUT,
    SEND;

    public static Operation map(String operation) {
        return Arrays.stream(Operation.values())
                .filter(o -> o.toString().equalsIgnoreCase(operation))
                .findFirst()
                .orElseThrow(() -> new SimpleChatException.UnsupportedOperation(operation));
    }
}
