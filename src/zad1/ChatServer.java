/**
 * @author Król Bartosz s31572
 */

package zad1;

import zad1.buffer.BufferService;
import zad1.buffer.ReadResultDto;
import zad1.exception.checked.ConnectionClosedException;
import zad1.exception.checked.InvalidMessageFormatException;
import zad1.exception.SimpleChatException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static zad1.buffer.BufferService.asBuffer;
import static zad1.CleaningUtils.closeChannelAndSelector;

public class ChatServer implements Runnable {

    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final Thread thread;
    private final BufferService bufferService;
    private final StringBuilder serverLog;
    private volatile boolean serverRunning = false;

    public ChatServer(String host, int port) {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(host, port));
            serverChannel.configureBlocking(false);
            selector = Selector.open();
            serverChannel.register(selector, OP_ACCEPT);
            thread = new Thread(this);
            bufferService = new BufferService();
            serverLog = new StringBuilder();
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
                    }

                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        try {
                            ReadResultDto result = bufferService.readFromChannel(channel);
                            switch (result.operation()) {
                                case LOGIN -> {
                                    //todo: login
                                }
                                case LOGOUT -> {
                                    //todo: logout
                                }
                                case SEND -> {
                                    //todo: send
                                }
                            }
                            //todo: aktualizuj serverLog
                        } catch (ConnectionClosedException e) {
                            //todo: wyloguj użytkownika
                        } catch (InvalidMessageFormatException ignored) { }
                    }

                    if (key.isWritable()) {

                    }
                }
            }
        } catch (Exception e) {
            throw new SimpleChatException.InternalServerError(e);
        } finally {
            closeChannelAndSelector(serverChannel, selector);
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
        return serverLog.toString();
    }

    private void broadcast(String message) throws CharacterCodingException {
        var iterator = selector.keys().iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            var session = (UserSessionDto) key.attachment();
            session.addToOutputQueue(asBuffer(message));
        }
    }
}
