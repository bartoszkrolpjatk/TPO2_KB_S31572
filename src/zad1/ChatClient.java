/**
 * @author Król Bartosz s31572
 */

package zad1;

public class ChatClient {

    private final String host;
    private final int port;
    private final String id;
    private final StringBuilder chatView = new StringBuilder();

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void login() {

    }

    public void send(String req) {

    }

    public String getChatView() {
        return chatView.toString();
    }
}
