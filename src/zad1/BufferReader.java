package zad1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class BufferReader {

    private final ByteBuffer buffer;
    private static final int BUFFER_CAPACITY = 1024;
    private static final CharsetDecoder decoder;
    private static final CharsetEncoder encoder;

    BufferReader() {
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

    public ReadResult readFromChannel(SocketChannel channel) throws IOException {
        buffer.clear();
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            return new ReadResult(true, null);
        }

        var data = new StringBuilder();
        while (bytesRead > 1) {
            buffer.flip();
            var charBuffer = decoder.decode(buffer);
            data.append(charBuffer);
            buffer.clear();
            bytesRead = channel.read(buffer);
        }

        return new ReadResult(false, data);
    }

    public record ReadResult(boolean connectionClosed, StringBuilder data) { }
}
