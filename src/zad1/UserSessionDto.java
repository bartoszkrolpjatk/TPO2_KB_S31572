package zad1;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

public class UserSessionDto {

    private static final Set<UserSessionDto> sessions = new LinkedHashSet<>();

    private final String id;
    private final Queue<ByteBuffer> outputQueue;
    private boolean halfClosed;

    public UserSessionDto(String id) {
        this.id = id;
        this.outputQueue = new ArrayDeque<>();
        sessions.add(this);
        this.halfClosed = false;
    }

    public void addToOutputQueue(ByteBuffer message) {
        outputQueue.add(message);
    }

    public void forget() {
        sessions.remove(this);
        halfClosed = true;
    }

    public ByteBuffer poll() {
        return outputQueue.poll();
    }

    public String id() {
        return id;
    }

    public boolean halfClosed() {
        return halfClosed;
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
