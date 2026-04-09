package zad1.exception.checked;

import zad1.buffer.ReadResultDto;

public class UserNotLoggedInException extends SimpleChatCheckedException {

    private final ReadResultDto messageNotSent;

    public UserNotLoggedInException(String message, ReadResultDto messageNotSent) {
        super(message);
        this.messageNotSent = messageNotSent;
    }

    public ReadResultDto messageNotSent() {
        return messageNotSent;
    }
}
