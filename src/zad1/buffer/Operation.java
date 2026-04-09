package zad1.buffer;

import java.util.Arrays;
import java.util.Optional;

public enum Operation {
    HI,
    BYE,
    SEND;

    static Optional<Operation> map(String operation) {
        return Arrays.stream(Operation.values())
                .filter(o -> o.toString().equalsIgnoreCase(operation))
                .findFirst();
    }
}
