/**
 * @author Król Bartosz s31572
 */

package zad1;

import zad1.buffer.BufferService;
import zad1.buffer.ReadResultDto;
import zad1.exception.SimpleChatException;
import zad1.exception.checked.ConnectionClosedException;
import zad1.exception.checked.UserNotLoggedInException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static zad1.CleaningUtils.closeChannelAndSelector;
import static zad1.SessionValidator.validateUserLoggedIn;
import static zad1.buffer.BufferService.asBuffer;

public class ChatServer implements Runnable {//todo: zwracanie błędów klientom

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
                            for (ReadResultDto result : bufferService.readFromChannel(channel)) {
                                var log = switch (result.operation()) {
                                    case HI -> {
                                        String id = result.message().strip();
                                        key.attach(new UserSessionDto(id));
                                        yield getFormattedLog(id, "logged in\n");
                                    }
                                    case BYE -> {
                                        var session = (UserSessionDto) key.attachment();
                                        validateUserLoggedIn(session, result);
                                        session.forget();
                                        yield getFormattedLog(session.id(), "logged out\n");
                                    }
                                    case SEND -> {
                                        var session = (UserSessionDto) key.attachment();
                                        validateUserLoggedIn(session, result);
                                        yield getFormattedLog(session.id() + ":", result.message());
                                    }
                                };
                                serverLog.append(log);
                                //todo: broadcast
                            }
                        } catch (ConnectionClosedException e) {
                            //todo: wyloguj użytkownika
                        } catch (UserNotLoggedInException e) {
                            System.out.printf("Not logged in user was trying to send message: %s. Request not executed.%n", e.messageNotSent());
                        }
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

    private String getFormattedLog(String id, String message) {//todo: wynieść do oddzielnej klasy
        var formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        return "%s %s %s".formatted(LocalTime.now().format(formatter), id, message);
    }
}
