package zad1;

import zad1.buffer.ReadResultDto;
import zad1.exception.checked.UserNotLoggedInException;

class SessionValidator {
    static void validateUserLoggedIn(UserSessionDto session, ReadResultDto result) throws UserNotLoggedInException {
        if (session == null) {
            throw new UserNotLoggedInException("User is not logged in!", result);
        }
    }
}
