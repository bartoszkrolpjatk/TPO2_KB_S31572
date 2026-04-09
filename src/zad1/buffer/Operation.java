package zad1.buffer;

import java.util.Arrays;
import java.util.Optional;

public enum Operation {
    LOGIN,
    LOGOUT,
    SEND;

    static Optional<Operation> map(String operation) {
        return Arrays.stream(Operation.values())
                .filter(o -> o.toString().equalsIgnoreCase(operation))
                .findFirst();
    }
}
