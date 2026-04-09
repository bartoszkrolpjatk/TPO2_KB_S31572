package zad1.buffer;

import zad1.exception.checked.ConnectionClosedException;
import zad1.exception.checked.InvalidMessageFormatException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

import static zad1.buffer.MessageValidator.validateMessageFormat;

public class BufferService {

    public static final String DELIMITER = ":";

    private final ByteBuffer buffer;
    private static final int BUFFER_CAPACITY = 1024;
    private static final CharsetDecoder decoder;
    private static final CharsetEncoder encoder;

    public BufferService() {
        buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
    }

    static {
        Charset cp1250 = Charset.forName("Cp1250");
        /* todo:zmienić na format UTF-8.
           Będzie to wymagać rozpatrzenia przypadku, kiedy dwa bajty mogą kodować jeden znak.
           Nie można ich rozdzielić.
           Potrzebny jest wtedy dodatkowy buffer czytający do znaku '\n'.*/
        decoder = cp1250.newDecoder();
        encoder = cp1250.newEncoder();
    }

    public List<ReadResultDto> readFromChannel(SocketChannel channel) throws IOException, ConnectionClosedException, InvalidMessageFormatException {
        buffer.clear();
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            throw new ConnectionClosedException("Connection closed by the client!");
        }

        var data = new StringBuilder();
        while (bytesRead > 0) {
            buffer.flip();
            var charBuffer = decoder.decode(buffer);
            data.append(charBuffer);
            buffer.clear();
            bytesRead = channel.read(buffer);
        }

        var rawMessages = data.toString().split("\n");
        var result = new ArrayList<ReadResultDto>();
        for (var rm : rawMessages) {
            try {
                result.add(validateMessageFormat(data.toString()));
            } catch (InvalidMessageFormatException e) {
                System.err.printf("Wrong message format. Cause: %s. Skipping...\n", e.getMessage());
            }
        }
        return result;
    }

    public static ByteBuffer asBuffer(String message) throws CharacterCodingException {
        return encoder.encode(CharBuffer.wrap(message));
    }
}
