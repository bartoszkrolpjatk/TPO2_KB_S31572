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

    public UserSessionDto(String id) {
        this.id = id;
        this.outputQueue = new ArrayDeque<>();
        sessions.add(this);
    }

    public void addToOutputQueue(ByteBuffer message) {
        outputQueue.add(message);
    }

    public void forget() {
        sessions.remove(this);
    }

    public String id() {
        return id;
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
