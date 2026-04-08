/**
 * @author Król Bartosz s31572
 */

package zad1;

import java.util.List;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<Void> {

    private ChatClient client;

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c, msgs, wait);
    }

    private ChatClientTask(ChatClient client, List<String> messages, int wait) {
        super(() -> {
            System.out.printf("Creating client task for %s%n", client);
            return null;
        });
        this.client = client;
    }

    public ChatClient getClient() {
        return client;
    }
}
