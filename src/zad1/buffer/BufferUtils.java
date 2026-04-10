package zad1.buffer;

import zad1.exception.checked.ConnectionClosedException;
import zad1.exception.checked.InvalidMessageFormatException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static zad1.buffer.MessageValidator.validateMessageFormat;

// todo: Zmienić na format UTF-8.
// todo: Będzie to wymagać rozpatrzenia przypadku, kiedy dwa bajty mogą kodować jeden znak.
// todo: Nie można ich rozdzielić. Potrzebny jest wtedy dodatkowy buffer czytający do znaku '\n'.
public class BufferUtils {

    public static final String DELIMITER = ":";

    private static final int BUFFER_CAPACITY = 1024;
    private static final Charset CP_1250 = Charset.forName("Cp1250");

    public static List<ReadResultDto> readFromChannel(SocketChannel channel) throws IOException, ConnectionClosedException {
        var buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            throw new ConnectionClosedException("Connection closed by the client!");
        }

        var data = new StringBuilder();
        while (bytesRead > 0) {
            buffer.flip();
            data.append(CP_1250.decode(buffer));
            buffer.clear();
            bytesRead = channel.read(buffer);
        }

        var rawMessages = data.toString().split("\n");
        var result = new ArrayList<ReadResultDto>();
        for (var rm : rawMessages) {
            try {
                result.add(validateMessageFormat(rm + "\n"));
            } catch (InvalidMessageFormatException e) {
                System.err.printf("Wrong message format: %s. Cause: %s. Skipping...\n",e.invalidFormatMessage(), e.getMessage());
            }
        }
        return result;
    }

    public static ByteBuffer asBuffer(String message) {
        return CP_1250.encode(message);
    }

    private BufferUtils() { }
}
