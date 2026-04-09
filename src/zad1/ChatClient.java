/**
 * @author Król Bartosz s31572
 */

package zad1;

import zad1.buffer.BufferService;
import zad1.exception.SimpleChatException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_READ;

public class ChatClient {

    private final String id;
    private final StringBuilder chatView = new StringBuilder();

    private final SocketChannel channel;
    private final Selector selector;
    private BroadcastListener broadcastListener;

    private static final String LOGIN_REQUEST = "hi:%s";
    private static final String LOGOUT_REQUEST = "bye:%s";
    private static final String SEND_REQUEST = "send:%s";

    public ChatClient(String host, int port, String id) {
        try {
            channel = SocketChannel.open(new InetSocketAddress(host, port));
            channel.configureBlocking(false);
            selector = Selector.open();
            this.id = id;
        } catch (IOException e) {
            throw new SimpleChatException.ClientCannotConnect(e);
        }
    }

    public void login() {
        try {
            send(LOGIN_REQUEST.formatted(id));
            channel.register(selector, OP_READ);
            startListeningForBroadcast();
        } catch (ClosedChannelException e) {
            throw new SimpleChatException.ClientCannotConnect(e);
        }
    }

    public void logout() {
        send(LOGOUT_REQUEST.formatted(id));
        broadcastListener.interrupt();
    }

    public void sendMessage(String message) {
        send(SEND_REQUEST.formatted(message));
    }

    public void send(String req) {
        try {
            channel.write(BufferService.asBuffer(req + '\n'));
        } catch (IOException e) {
            throw new SimpleChatException.SendingMessageFailed(e);
        }
    }

    private void startListeningForBroadcast() {
        if (broadcastListener != null)
            throw new SimpleChatException.ListeningToBroadcastFailed("Client %s is already listening to server broadcast!".formatted(this));

        broadcastListener = new BroadcastListener(channel, selector, chatView);
        this.broadcastListener.start();
    }

    public String getChatView() {
        return chatView.toString();
    }

    @Override
    public String toString() {
        return "[Client %s]".formatted(id);
    }
}
