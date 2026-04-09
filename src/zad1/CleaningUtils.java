package zad1;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractInterruptibleChannel;

public class CleaningUtils {

    public static void closeChannelAndSelector(AbstractInterruptibleChannel channel, Selector selector) {
        try {
            selector.close();
            channel.close();
        } catch (IOException e) {
            throw new SimpleChatException.CloseFailed(e);
        }
    }

    private CleaningUtils() { }
}
