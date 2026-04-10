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

//todo: BufferService powinien być wspólny dla całego serwera

//todo: Przygotować BufferService na zagłodzenie wątków, trzeba zrobić mechanizm kontroli wielkości wiadomości danego klienta
//todo: Należy czytać wiadomość do pewnego limitu, a następnie schować do schowka użytkownika to co się tam nie zmieściło
public class BufferService {

    public static final String DELIMITER = ":";
    private static final int BUFFER_CAPACITY = 1024;

    private final ByteBuffer buffer;
    private final CharsetDecoder decoder;
    private final CharsetEncoder encoder;

    public BufferService() {
        buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
        Charset cp1250 = Charset.forName("Cp1250");
        /* todo:zmienić na format UTF-8.
           Będzie to wymagać rozpatrzenia przypadku, kiedy dwa bajty mogą kodować jeden znak.
           Nie można ich rozdzielić.
           Potrzebny jest wtedy dodatkowy buffer czytający do znaku '\n'.*/
        decoder = cp1250.newDecoder();
        encoder = cp1250.newEncoder();
    }

    public List<ReadResultDto> readFromChannel(SocketChannel channel) throws IOException, ConnectionClosedException {
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
                result.add(validateMessageFormat(rm + "\n"));
            } catch (InvalidMessageFormatException e) {
                System.err.printf("Wrong message format: %s. Cause: %s. Skipping...\n",e.invalidFormatMessage(), e.getMessage());
            }
        }
        return result;
    }

    public ByteBuffer asBuffer(String message) throws CharacterCodingException {
        return encoder.encode(CharBuffer.wrap(message));
    }
}
