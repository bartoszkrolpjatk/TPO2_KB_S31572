package zad1.buffer;

public record ReadResultDto(Operation operation, String message) {
    @Override
    public String toString() {
        return "Message: %s %s".formatted(operation, message);
    }
}
