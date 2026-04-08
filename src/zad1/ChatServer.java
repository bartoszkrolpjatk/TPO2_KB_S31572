/**
 * @author Król Bartosz s31572
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

public class ChatServer implements Runnable {

    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final Thread thread;
    private volatile boolean serverRunning = false;

    public ChatServer(String host, int port) {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(host, port));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, OP_ACCEPT);
            thread = new Thread(this);
        } catch (IOException e) {
            throw new SimpleChatException.ServerStartFailed(e);
        }
    }

    @Override
    public void run() {
        serverRunning = true;
        while (serverRunning) {
            try {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                var iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        SocketChannel newClientChannel = serverChannel.accept();
                        newClientChannel.configureBlocking(false);
                        newClientChannel.register(selector, OP_READ | OP_WRITE);
                    } else if (key.isReadable()) {

                    } else if (key.isWritable()) {

                    }
                }
            } catch (IOException e) {
                throw new SimpleChatException.InternalServerError(e);
            }
        }
    }

    public void startServer() {
        thread.start();
    }

    public void stopServer() {
        try {
            serverRunning = false;
            selector.wakeup();
            selector.close();
            serverChannel.close();
            thread.interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getServerLog() {
        return "Server log test";
    }
}
