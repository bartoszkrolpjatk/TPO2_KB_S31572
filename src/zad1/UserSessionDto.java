package zad1;

import java.nio.ByteBuffer;
import java.util.PriorityQueue;
import java.util.Queue;

public class UserSessionDto {

    private final String id;
    private final Queue<ByteBuffer> outputQueue;

    public UserSessionDto(String id) {
        this.id = id;
        this.outputQueue = new PriorityQueue<>();
    }

    public void addToOutputQueue(ByteBuffer message) {
        outputQueue.add(message);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        UserSessionDto that = (UserSessionDto) object;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
