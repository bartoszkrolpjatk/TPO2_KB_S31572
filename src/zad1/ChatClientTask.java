/**
 * @author Król Bartosz s31572
 */

package zad1;

import java.util.List;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<Void> {

    private final ChatClient client;

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c, msgs, wait);
    }

    private ChatClientTask(ChatClient client, List<String> messages, int wait) {
        super(() -> {
            client.login();
            for (var msg : messages) {
                client.send(msg);
                Thread.sleep(wait);
            }
            client.logout();
            return null;
        });
        this.client = client;
    }

    public ChatClient getClient() {
        return client;
    }
}
