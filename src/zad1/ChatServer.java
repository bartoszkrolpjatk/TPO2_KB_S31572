/**
 * @author Król Bartosz s31572
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class ChatServer implements Runnable {

    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final Thread thread;
    private volatile boolean serverRunning = false;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

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
        try {
            while (serverRunning) {
                selector.select();

                if (!serverRunning) {
                    break;
                }

                var iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        SocketChannel newClientChannel = serverChannel.accept();
                        newClientChannel.configureBlocking(false);
                        newClientChannel.register(selector, OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        buffer.clear();
                        int bytesRead = channel.read(buffer);

                        while (bytesRead > 1) {
                            buffer.flip();
                            var charBuffer = Charset.forName("Cp1250").decode(buffer);
//                            System.out.println(charBuffer);
                            buffer.clear();
                            bytesRead = channel.read(buffer);
                        }
                    } else if (key.isWritable()) {

                    }
                }
            }
        } catch (IOException e) {
            throw new SimpleChatException.InternalServerError(e);
        } finally {
            try {
                selector.close();
                serverChannel.close();
            } catch (IOException e) {
                throw new RuntimeException(e);//todo: wyjątek
            }
        }

    }

    public void startServer() {
        serverRunning = true;
        thread.start();
    }

    public void stopServer() {
        serverRunning = false;
        selector.wakeup();
    }

    public String getServerLog() {
        return "Server log test";
    }
}
