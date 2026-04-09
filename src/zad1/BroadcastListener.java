package zad1;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static zad1.CleaningUtils.closeChannelAndSelector;

public class BroadcastListener implements Runnable {

    private final BufferReader bufferReader;
    private final SocketChannel channel;
    private final Selector selector;
    private final StringBuilder chatView;
    private final Thread thread;
    private volatile boolean listeningToBroadcast;

    public BroadcastListener(SocketChannel channel, Selector selector, StringBuilder chatView) {
        this.channel = channel;
        this.bufferReader = new BufferReader();
        this.selector = selector;
        this.chatView = chatView;
        this.listeningToBroadcast = false;
        this.thread = new Thread(this);
    }

    @Override
    public void run() {
        try {
            while (listeningToBroadcast) {
                selector.select();
                var iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        BufferReader.ReadResult result = bufferReader.readFromChannel(channel);
                        if (result.connectionClosed()) {
                            listeningToBroadcast = false;
                            continue;
                        }
                        chatView.append(result.data());
                    }
                }
            }
        } catch (IOException e) {
            throw new SimpleChatException.ListeningToBroadcastFailed("For client %s. Exception while listening to broadcast: %s.".formatted(this, e.getMessage()));
        } finally {
            closeChannelAndSelector(channel, selector);
        }
    }

    public void start() {
        listeningToBroadcast = true;
        thread.start();
    }

    public void interrupt() {
        listeningToBroadcast = false;
        thread.interrupt();
    }
}
