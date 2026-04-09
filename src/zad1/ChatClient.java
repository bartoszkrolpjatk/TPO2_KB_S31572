/**
 * @author Król Bartosz s31572
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class ChatClient {

    private final String id;
    private final StringBuilder chatView = new StringBuilder();

    private final SocketChannel channel;
    private final Selector selector;
    private final SelectionKey key;
    private final BufferReader bufferReader;

    private Thread broadcastListener;
    private volatile boolean listeningToBroadcast = false;

    private static final String LOGIN_REQUEST = "hi:%s";
    private static final String LOGOUT_REQUEST = "bye:%s";

    public ChatClient(String host, int port, String id) {
        try {
            channel = SocketChannel.open(new InetSocketAddress(host, port));
            channel.configureBlocking(false);
            selector = Selector.open();
            key = channel.register(selector, OP_WRITE);
            this.id = id;
            this.bufferReader = new BufferReader();
        } catch (IOException e) {
            throw new SimpleChatException.ClientCannotConnect(e);
        }
    }

    public void send(String req) {
        var buffer = Charset.forName("Cp1250").encode(req + '\n');
        try {
            channel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void login() {
        send(LOGIN_REQUEST.formatted(id));
        key.interestOps(key.interestOps() | OP_READ);
        startListeningForBroadcast();
    }

    public void logout() {
        send(LOGOUT_REQUEST.formatted(id));
        listeningToBroadcast = false;
        try {
            channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);//todo: wyjątek
        }
    }

    private void startListeningForBroadcast() {
        if (broadcastListener != null)
            throw new SimpleChatException.ListeningToBroadcastFailed("Client %s is already listening to server broadcast!".formatted(this));

        listeningToBroadcast = true;
        broadcastListener = new Thread(() -> {
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
                try {
                    selector.close();
                } catch (IOException e) {
                    throw new RuntimeException(e); //todo: wyjątek
                }
            }
        });
        broadcastListener.start();
    }

    public String getChatView() {
        return chatView.toString();
    }

    @Override
    public String toString() {
        return "[Client %s]".formatted(id);
    }
}
