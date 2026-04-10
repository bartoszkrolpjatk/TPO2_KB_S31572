/**
 * @author Król Bartosz s31572
 */

package zad1;

import zad1.buffer.Operation;
import zad1.buffer.ReadResultDto;
import zad1.exception.SimpleChatException;
import zad1.exception.checked.ConnectionClosedException;
import zad1.exception.checked.UserNotLoggedInException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static zad1.CleaningUtils.closeChannelAndSelector;
import static zad1.SessionValidator.validateUserLoggedIn;
import static zad1.buffer.BufferUtils.asBuffer;
import static zad1.buffer.BufferUtils.readFromChannel;

//todo: zwracanie błędów klientom
public class ChatServer implements Runnable {

    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final Thread thread;
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
                            for (ReadResultDto result : readFromChannel(channel)) {
                                Messages messages = switch (result.operation()) {
                                    case HI -> {
                                        String id = result.message().strip();
                                        key.attach(new UserSessionDto(id));
                                        yield new Messages(id, "logged in\n");
                                    }
                                    case BYE -> {
                                        var session = (UserSessionDto) key.attachment();
                                        validateUserLoggedIn(session, result);
                                        yield new Messages(session.id(), "logged out\n");
                                    }
                                    case SEND -> {
                                        var session = (UserSessionDto) key.attachment();
                                        validateUserLoggedIn(session, result);
                                        yield new Messages(session.id() + ":", result.message());
                                    }
                                    case EVENT -> throw new SimpleChatException.UnexpectedOperationException("Client cannot broadcast messages!");
                                };
                                serverLog.append(messages.serverLog());
                                broadcast(messages.broadcastMessage());

                                if (result.operation() == Operation.BYE) {
                                    var session = (UserSessionDto) key.attachment();
                                    session.forget();
                                }
                            }
                        } catch (ConnectionClosedException e) {
                            //todo: wyloguj użytkownika
                        } catch (UserNotLoggedInException e) {
                            System.out.printf("Not logged in user was trying to send message: %s. Request not executed.%n", e.messageNotSent());
                        }
                    }

                    if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        UserSessionDto session = (UserSessionDto) key.attachment();

                        ByteBuffer broadcastMessage;
                        while ((broadcastMessage = session.poll()) != null) {
                            channel.write(broadcastMessage);
                        }

                        key.interestOps(key.interestOps() & ~OP_WRITE);
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
        System.out.println("Server started");
    }

    public void stopServer() {
        serverRunning = false;
        selector.wakeup();
        System.out.println("Server stopped");
    }

    public String getServerLog() {
        return serverLog.toString();
    }

    private void broadcast(String message) {
        for (SelectionKey key : selector.keys()) {
            var session = (UserSessionDto) key.attachment();
            if (session == null || session.halfClosed()) continue;

            session.addToOutputQueue(asBuffer("event:%s".formatted(message)));
            key.interestOps(key.interestOps() | OP_WRITE);
        }
    }
}
